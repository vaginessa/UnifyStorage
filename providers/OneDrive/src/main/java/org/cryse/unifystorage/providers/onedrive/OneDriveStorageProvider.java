package org.cryse.unifystorage.providers.onedrive;

import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.cryse.unifystorage.AbstractStorageProvider;
import org.cryse.unifystorage.ConflictBehavior;
import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.RemoteFileDownloader;
import org.cryse.unifystorage.FileUpdater;
import org.cryse.unifystorage.HashAlgorithm;
import org.cryse.unifystorage.StorageException;
import org.cryse.unifystorage.StorageUserInfo;
import org.cryse.unifystorage.providers.onedrive.model.RefreshToken;
import org.cryse.unifystorage.utils.DirectoryInfo;
import org.cryse.unifystorage.utils.OperationResult;
import org.cryse.unifystorage.utils.Path;
import org.cryse.unifystorage.utils.ProgressCallback;
import org.cryse.unifystorage.utils.hash.Sha1HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OneDriveStorageProvider extends AbstractStorageProvider {
    public static final String NAME = OneDriveConst.NAME_STORAGE_PROVIDER;
    private String mClientId;
    private OneDriveFile mRootFile;
    private OneDriveCredential mOneDriveCredential;
    private OneDriveUserInfo mOwnerUserInfo;
    private String mAuthenticationHeader = "";
    private Gson gson = GsonFactory.getGsonInstance();

    OkHttpClient mOkHttpClient = null;
    Retrofit mRetrofit = null;
    OneDriveService mOneDriveService;

    /*public OneDriveStorageProvider(String clientId, String redirectUri, String clientSecret, final OneDriveCredential credential) {
        mClientId = clientId;
        mRedirectUri = redirectUri;
        mClientSecret = clientSecret;
        mOneDriveCredential = credential;
        if (mOneDriveCredential != null)
            mAuthenticationHeader = "Bearer " + mOneDriveCredential.getAccessToken();
        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://" + OneDriveService.SUBDOMAIN_API + OneDriveService.SUBDOMAIN_VERSION)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mOneDriveService = retrofit.create(OneDriveService.class);
    }*/

    public OneDriveStorageProvider(OkHttpClient okHttpClient, OneDriveCredential credential, String clientId) {
        this.mClientId = clientId;
        this.mOneDriveCredential = credential;
        if (this.mOneDriveCredential != null)
            this.mAuthenticationHeader = "Bearer " + mOneDriveCredential.getAccessToken();

        this.mOkHttpClient = okHttpClient;
        this.mRetrofit = new Retrofit.Builder()
                .baseUrl("https://" + OneDriveService.SUBDOMAIN_API + OneDriveService.SUBDOMAIN_VERSION)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.mOneDriveService = mRetrofit.create(OneDriveService.class);
    }

    private void checkCredential() {
        if(mOneDriveCredential.isAvailable() ) {
            if(mOneDriveCredential.isExpired()) {
                refreshToken();
            }
        } else {
            throw new StorageException();
        }
    }

    private void refreshToken() {
        Call<JsonObject> refreshCall = mOneDriveService.refreshToken(
                mClientId,
                null,
                null,
                mOneDriveCredential.getRefreshToken(),
                "refresh_token"
        );
        try {
            Response<JsonObject> response = refreshCall.execute();
            int responseCode = response.code();
            JsonObject responseObject = response.body();
            if (responseCode == 200) {
                RefreshToken refreshToken = gson.fromJson(responseObject, RefreshToken.class);
                if(refreshToken != null) {
                    mOneDriveCredential.setAccessToken(refreshToken.accessToken);
                    mOneDriveCredential.setRefreshToken(refreshToken.refreshToken);
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.SECOND, refreshToken.expiresIn - 60);
                    mOneDriveCredential.setExpiresIn(calendar.getTime());
                    mAuthenticationHeader = "Bearer " + mOneDriveCredential.getAccessToken();
                    if(mOnTokenRefreshListener != null)
                        mOnTokenRefreshListener.onTokenRefresh(mOneDriveCredential);
                }
            } else {
                // Failure here
                throw new StorageException();
            }
        } catch (IOException ex) {
            throw new StorageException();
        }
    }

    @Override
    public String getStorageProviderName() {
        return OneDriveConst.NAME_STORAGE_PROVIDER;
    }

    @Override
    public RemoteFile getRootDirectory() throws StorageException {
        if(mRootFile == null) {
            checkCredential();
            Call<JsonObject> call = mOneDriveService.getDriveRoot(mAuthenticationHeader);
            try {
                Response<JsonObject> response = call.execute();
                int responseCode = response.code();
                JsonObject responseObject = response.body();
                if (responseCode == 200) {
                    mRootFile = gson.fromJson(responseObject, OneDriveFile.class);
                } else {
                    // Failure here
                    throw new StorageException();
                }
                //String resultString = responseObject.toString();
            } catch (IOException ex) {
                throw new StorageException(ex);
            }
        }
        return mRootFile;
    }

    @Override
    public DirectoryInfo list(DirectoryInfo directoryInfo) throws StorageException {
        RemoteFile directory = directoryInfo.directory;
        List<RemoteFile> list = new ArrayList<>();
        boolean hasMore = false;
        String cursor = null;
        checkCredential();
        JsonObject responseJsonObject = null;
        boolean append = directoryInfo.hasMore;
        if(directoryInfo.hasMore && !TextUtils.isEmpty(directoryInfo.cursor)) {
            String url = directoryInfo.cursor;
            Request request = new Request.Builder().url(url).addHeader("Authorization", mAuthenticationHeader).build();
            try {
                okhttp3.Response response = mOkHttpClient.newCall(request).execute();
                if(response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    String responseString = responseBody.string();
                    JsonParser parser = new JsonParser();
                    responseJsonObject = parser.parse(responseString).getAsJsonObject();
                }
            } catch (IOException e) {
                throw new StorageException(e);
            }
        } else {
            Call<JsonObject> call = mOneDriveService.listChildrenById(mAuthenticationHeader, directory.getId());
            try {
                Response<JsonObject> response = call.execute();
                int responseCode = response.code();
                if (responseCode == 200) {
                    responseJsonObject = response.body();
                } else {
                    // Failure here
                }
                //String resultString = responseObject.toString();
            } catch (IOException e) {
                throw new StorageException(e);
            }
        }

        if(responseJsonObject != null) {
            if (responseJsonObject.has("value")) {
                List<OneDriveFile> fileMetas = gson.fromJson(responseJsonObject.get("value"), new TypeToken<List<OneDriveFile>>() {
                }.getType());
                list.addAll(fileMetas);
            }
            if(responseJsonObject.has("@odata.nextLink") && !TextUtils.isEmpty(responseJsonObject.get("@odata.nextLink").getAsString())) {
                hasMore = true;
                cursor = responseJsonObject.get("@odata.nextLink").getAsString();
            }
            if(!append) {
                directoryInfo.files.clear();
            }
            directoryInfo.files.addAll(list);
            directoryInfo.hasMore = hasMore;
            directoryInfo.cursor = cursor;
        }
        return directoryInfo;
    }

    @Override
    public RemoteFile createDirectory(RemoteFile parent, String name) throws StorageException {
        OneDriveFile fileMetaData = null;
        JsonObject requestData = new RequestDataBuilder()
                .createFolder(name, "fail")
                .build();
        Call<JsonObject> call = mOneDriveService.createFolder(mAuthenticationHeader, parent.getId(), requestData);
        try {
            Response<JsonObject> response = call.execute();
            int responseCode = response.code();
            JsonObject responseObject = response.body();
            if (responseCode == 201) {
                fileMetaData = gson.fromJson(responseObject, OneDriveFile.class);
            } else {
                // Failure here
                throw new StorageException();
            }
            //String resultString = responseObject.toString();
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
        return fileMetaData;
    }

    @Override
    public RemoteFile createFile(RemoteFile parent, String name, InputStream input, ConflictBehavior behavior) throws StorageException {
        try {
            /*final Option option = new QueryOption("@name.conflictBehavior", conflictBehaviorToString(behavior));
            Item newItem = mOneDriveClient
                    .getDrive()
                    .getItems(parent.getId())
                    .getChildren()
                    .byId(name)
                    .getContent()
                    .buildRequest(Collections.singletonList(option))
                    .put(IOUtils.toByteArray(input));*/
            return null; // new OneDriveFile(newItem);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public boolean exists(RemoteFile parent, String name) throws StorageException {
        OneDriveFile fileMetaData = null;
        Call<JsonObject> call = mOneDriveService.getMetaDataByPath(mAuthenticationHeader, Path.combine(parent.getPath(), name));
        try {
            Response<JsonObject> response = call.execute();
            int responseCode = response.code();
            JsonObject responseObject = response.body();
            if (responseCode == 200) {
                fileMetaData = gson.fromJson(responseObject, OneDriveFile.class);
                return true;
            } else {
                // Failure here
            }
            //String resultString = responseObject.toString();
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
        return false;
    }

    @Override
    public RemoteFile getFile(RemoteFile parent, String name) throws StorageException {
        OneDriveFile fileMetaData = null;
        Call<JsonObject> call = mOneDriveService.getMetaDataByPath(mAuthenticationHeader, Path.combine(parent.getPath(), name));
        try {
            Response<JsonObject> response = call.execute();
            int responseCode = response.code();
            JsonObject responseObject = response.body();
            if (responseCode == 200) {
                fileMetaData = gson.fromJson(responseObject, OneDriveFile.class);
            } else {
                // Failure here
                throw new StorageException();
            }
            //String resultString = responseObject.toString();
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
        return fileMetaData;
    }

    @Override
    public OneDriveFile getFileById(String id) throws StorageException {
        try {
            /*Item item = mOneDriveClient
                    .getDrive()
                    .getItems(id)
                    .buildRequest()
                    .get();*/
            return null; // new OneDriveFile(item);
        } catch (Throwable throwable) {
            throw new StorageException(throwable);
        }
    }

    @Override
    public OneDriveFile updateFile(RemoteFile remote, InputStream input, FileUpdater updater) throws StorageException {
        try {
            /*final Option option = new QueryOption("@name.conflictBehavior", conflictBehaviorToString(ConflictBehavior.REPLACE));
            Item newItem = mOneDriveClient
                    .getDrive()
                    .getItems(remote.getId())
                    .getChildren()
                    .byId(remote.getId())
                    .getContent()
                    .buildRequest(Collections.singletonList(option))
                    .put(IOUtils.toByteArray(input));*/
            return null; // new OneDriveFile(newItem);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public OperationResult deleteFile(RemoteFile file) {
        OneDriveFile fileMetaData = null;
        Call<JsonObject> call = mOneDriveService.deleteById(mAuthenticationHeader, file.getId());
        try {
            Response<JsonObject> response = call.execute();
            int responseCode = response.code();
            JsonObject responseObject = response.body();
            if (responseCode == 204) {
                return OperationResult.create(file, true);
            } else {
                // Failure here
                return OperationResult.create(file, false);
            }
            //String resultString = responseObject.toString();
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public void copyFile(RemoteFile target, final ProgressCallback callback, RemoteFile...files) throws StorageException {
        /*final ItemReference parentReference = new ItemReference();
        parentReference.id = target.getId();

        final IProgressCallback<Item> progressCallback = new IProgressCallback<Item>() {
            @Override
            public void progress(final long current, final long max) {
                if(callback != null) {
                    callback.onProgress(current, max);
                }
            }

            @Override
            public void success(final Item item) {
                if (callback != null) {
                    callback.onSuccess();
                }
            }

                @Override
                public void failure ( final ClientException error){
                    if (callback != null) {
                        callback.onFailure(error);
                    }
                }
            };

        final ICallback<AsyncMonitor<Item>> copyCallback
                = new ICallback<AsyncMonitor<Item>>() {
            @Override
            public void success(final AsyncMonitor<Item> itemAsyncMonitor) {
                final int millisBetweenPoll = 1000;
                itemAsyncMonitor.pollForResult(millisBetweenPoll, progressCallback);
            }

            @Override
            public void failure(ClientException ex) {

            }
        };

        mOneDriveClient
                .getDrive()
                .getItems(file.getId())
                .getCopy(file.getName(), parentReference)
                .buildRequest()
                .create(copyCallback);*/
    }

    @Override
    public void moveFile(RemoteFile targe, final ProgressCallback callback, RemoteFile...files) throws StorageException {

    }

    @Override
    public RemoteFile getFileDetail(RemoteFile file) throws StorageException {
        return null;
    }

    @Override
    public RemoteFile getFilePermission(RemoteFile file) throws StorageException {
        return null;
    }

    @Override
    public RemoteFile updateFilePermission(RemoteFile file) throws StorageException {
        return null;
    }

    @Override
    public StorageUserInfo getUserInfo(boolean forceRefresh) throws StorageException {
        try {
            /*if(mOwnerUserInfo == null || forceRefresh) {
                Drive drive = mOneDriveClient.getDrive().buildRequest().get();
                mOwnerUserInfo = new OneDriveUserInfo(drive.owner);
            }*/
            return null; // mOwnerUserInfo;
        } catch (Throwable throwable) {
            throw new StorageException(throwable);
        }
    }

    @Override
    public HashAlgorithm getHashAlgorithm() {
        return Sha1HashAlgorithm.getInstance();
    }

    private static String conflictBehaviorToString(ConflictBehavior behavior) {
        switch (behavior) {
            case RENAME:
                return "rename";
            case REPLACE:
                return "replace";
            case FAIL:
            default:
                return "fail";
        }
    }

    @Override
    public RemoteFileDownloader download(RemoteFile file) throws StorageException {
        checkCredential();
        Call<ResponseBody> call = mOneDriveService.downloadById(mAuthenticationHeader, file.getId());
        try {
            Response<ResponseBody> response = call.execute();
            int responseCode = response.code();
            ResponseBody responseBody = response.body();
            if (responseCode == 200) {
                return new RemoteFileDownloader(file, responseBody.byteStream());
            } else {
                // Failure here
                throw new StorageException();
            }
            //String resultString = responseObject.toString();
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
    }
}
