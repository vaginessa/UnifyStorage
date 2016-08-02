package org.cryse.unifystorage.providers.dropbox;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.cryse.unifystorage.AbstractStorageProvider;
import org.cryse.unifystorage.ConflictBehavior;
import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.RemoteFileDownloader;
import org.cryse.unifystorage.FileUpdater;
import org.cryse.unifystorage.HashAlgorithm;
import org.cryse.unifystorage.StorageException;
import org.cryse.unifystorage.StorageUserInfo;
import org.cryse.unifystorage.utils.OperationResult;
import org.cryse.unifystorage.utils.DirectoryInfo;
import org.cryse.unifystorage.utils.Path;
import org.cryse.unifystorage.utils.ProgressCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DropboxStorageProvider extends AbstractStorageProvider {
    // private DbxClientV2 mDropboxClient;
    private DropboxCredential mDropboxCredential;
    private DropboxFile mRootFile;
    private Gson gson = new Gson();
    private String mAuthenticationHeader = "";

    public static final String NAME = DropboxConst.NAME_STORAGE_PROVIDER;

    OkHttpClient mOkHttpClient = null;
    Retrofit mRetrofit;
    DropboxService mDropboxService;

    public DropboxStorageProvider(OkHttpClient okHttpClient, DropboxCredential credential, String clientIdentifier) {
        this.mDropboxCredential = credential;
        if (this.mDropboxCredential != null)
            this.mAuthenticationHeader = "Bearer " + this.mDropboxCredential.getAccessSecret();

        this.mOkHttpClient = okHttpClient;
        this.mRetrofit = new Retrofit.Builder()
                .baseUrl("https:" + DropboxService.SUBDOMAIN_API)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.mDropboxService = mRetrofit.create(DropboxService.class);
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public String getStorageProviderName() {
        return DropboxConst.NAME_STORAGE_PROVIDER;
    }

    @Override
    public RemoteFile getRootDirectory() throws StorageException {
        if (mRootFile == null) {
            mRootFile = new DropboxFile();
        }
        return mRootFile;
    }

    @Override
    public DirectoryInfo list(DirectoryInfo directoryInfo) throws StorageException {
        /*try {*/
        RemoteFile directory = directoryInfo.directory;
        List<RemoteFile> list = new ArrayList<RemoteFile>();
        String parentPath = getPathString(directory);
        boolean hasMore = false;
        String cursor = null;
        boolean append = directoryInfo.hasMore;
        JsonObject responseJsonObject = null;
        if(directoryInfo.hasMore && !TextUtils.isEmpty(directoryInfo.cursor)) {
            String moreCursor = directoryInfo.cursor;
            JsonObject requestMoreData = new DropboxRequestDataBuilder()
                    .listFolderContinue(moreCursor)
                    .build();
            Call<JsonObject> moreCall = mDropboxService.listFoldersContinue(mAuthenticationHeader, requestMoreData);
            Response<JsonObject> moreResponse = null;
            try {
                moreResponse = moreCall.execute();
                if (moreResponse.code() == 200) {
                    responseJsonObject = moreResponse.body();
                }
            } catch (IOException e) {
                throw new StorageException(e);
            }
        } else {
            JsonObject requestData = new DropboxRequestDataBuilder()
                    .listFolder(parentPath)
                    .build();
            Call<JsonObject> call = mDropboxService.listFolders(mAuthenticationHeader, requestData);
            try {
                Response<JsonObject> response = call.execute();
                int responseCode = response.code();
                if (responseCode == 200) {
                    responseJsonObject = response.body();
                }
            } catch (IOException e) {
                throw new StorageException(e);
            }
        }

        if(responseJsonObject != null) {
            if (responseJsonObject.has("entries")) {
                List<DropboxFile> fileMetas = gson.fromJson(responseJsonObject.get("entries"), new TypeToken<List<DropboxFile>>() {
                }.getType());
                list.addAll(fileMetas);
            }
            if(responseJsonObject.has("has_more") && responseJsonObject.get("has_more").getAsBoolean() && responseJsonObject.has("cursor")) {
                hasMore = true;
                cursor = responseJsonObject.get("cursor").getAsString();
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
    public DropboxFile createDirectory(RemoteFile parent, String name) throws StorageException {
        if(parent == null)
            parent = getRootDirectory();
        DropboxFile fileMetaData = null;
        JsonObject requestData = new DropboxRequestDataBuilder()
                .createFolder(Path.combine(parent.getPath(), name))
                .build();
        Call<JsonObject> call = mDropboxService.createFolder(mAuthenticationHeader, requestData);
        try {
            Response<JsonObject> response = call.execute();
            int responseCode = response.code();
            JsonObject responseObject = response.body();
            if (responseCode == 200) {
                fileMetaData = gson.fromJson(responseObject.get("entries"), DropboxFile.class);
            } else {
                // Failure here
            }
            //String resultString = responseObject.toString();
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
        return fileMetaData;
    }

    @Override
    public DropboxFile createFile(RemoteFile parent, String name, InputStream input, ConflictBehavior behavior) throws StorageException {
        return null;
    }

    @Override
    public boolean exists(RemoteFile parent, String name) throws StorageException {
        DropboxFile fileMetaData = null;
        JsonObject requestData = new DropboxRequestDataBuilder()
                .getMetaData(Path.combine(parent.getPath(), name))
                .build();
        Call<JsonObject> call = mDropboxService.getMetaData(mAuthenticationHeader, requestData);
        try {
            Response<JsonObject> response = call.execute();
            int responseCode = response.code();
            JsonObject responseObject = response.body();
            if (responseCode == 200) {
                fileMetaData = gson.fromJson(responseObject, DropboxFile.class);
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
    public DropboxFile getFile(RemoteFile parent, String name) throws StorageException {
        return getFile(Path.combine(parent.getPath(), name));
    }

    @Override
    public DropboxFile getFile(String path) throws StorageException {
        DropboxFile fileMetaData = null;
        JsonObject requestData = new DropboxRequestDataBuilder()
                .getMetaData(path)
                .build();
        Call<JsonObject> call = mDropboxService.getMetaData(mAuthenticationHeader, requestData);
        try {
            Response<JsonObject> response = call.execute();
            int responseCode = response.code();
            JsonObject responseObject = response.body();
            if (responseCode == 200) {
                fileMetaData = gson.fromJson(responseObject, DropboxFile.class);
            } else {
                // Failure here
            }
            //String resultString = responseObject.toString();
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
        return fileMetaData;
    }

    @Override
    public DropboxFile getFileById(String id) throws StorageException {
        DropboxFile fileMetaData = null;
        JsonObject requestData = new DropboxRequestDataBuilder()
                .getMetaData(id)
                .build();
        Call<JsonObject> call = mDropboxService.getMetaData(mAuthenticationHeader, requestData);
        try {
            Response<JsonObject> response = call.execute();
            int responseCode = response.code();
            JsonObject responseObject = response.body();
            if (responseCode == 200) {
                fileMetaData = gson.fromJson(responseObject, DropboxFile.class);
            } else {
                // Failure here

            }
            //String resultString = responseObject.toString();
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
        return fileMetaData;
    }

    @Override
    public DropboxFile updateFile(RemoteFile remote, InputStream input, FileUpdater updater) throws StorageException {
        return null;
    }

    @Override
    public OperationResult deleteFile(RemoteFile file) {
        DropboxFile fileMetaData = null;
        JsonObject requestData = new DropboxRequestDataBuilder()
                .delete(file.getPath())
                .build();
        Call<JsonObject> call = mDropboxService.delete(mAuthenticationHeader, requestData);
        try {
            Response<JsonObject> response = call.execute();
            int responseCode = response.code();
            JsonObject responseObject = response.body();
            if (responseCode == 200) {
                fileMetaData = gson.fromJson(responseObject, DropboxFile.class);
            } else {
                // Failure here
                return OperationResult.create((RemoteFile) file, false);
            }
            //String resultString = responseObject.toString();
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
        return OperationResult.create((RemoteFile) file, null != fileMetaData);
    }

    @Override
    public void copyFile(RemoteFile targetParent, RemoteFile file) throws StorageException {

    }

    @Override
    public void copyFile(RemoteFile targetParent, RemoteFile file, ProgressCallback callback) throws StorageException {

    }

    @Override
    public void moveFile(RemoteFile targetParent, RemoteFile file) throws StorageException {

    }

    @Override
    public void moveFile(RemoteFile targetParent, RemoteFile file, ProgressCallback callback) throws StorageException {

    }

    @Override
    public DropboxFile getFileDetail(RemoteFile file) throws StorageException {
        return null;
    }

    @Override
    public DropboxFile getFilePermission(RemoteFile file) throws StorageException {
        return null;
    }

    @Override
    public DropboxFile updateFilePermission(RemoteFile file) throws StorageException {
        return null;
    }

    @Override
    public StorageUserInfo getUserInfo(boolean forceRefresh) throws StorageException {
        return null;
    }

    @Override
    public Request download(RemoteFile file) throws StorageException {
        JsonObject requestData = new DropboxRequestDataBuilder()
                .download(file.getPath())
                .build();
        return mDropboxService.download(mAuthenticationHeader, requestData.toString()).request();
        /*try {
            Response<ResponseBody> response = call.execute();
            int responseCode = response.code();
            ResponseBody responseBody = response.body();
            if (responseCode == 200) {
                return responseBody.byteStream();
            } else {
                // Failure here
                throw new StorageException();
            }
            //String resultString = responseObject.toString();
        } catch (IOException ex) {
            throw new StorageException(ex);
        }*/
    }

    @Override
    public HashAlgorithm getHashAlgorithm() {
        return null;
    }

    private static String getPathString(RemoteFile file) {
        if(file == null) return "";
        else if(file.getPath() == null) return "";
        else return file.getPath().equalsIgnoreCase("/") ? "" : file.getPath();
    }
}
