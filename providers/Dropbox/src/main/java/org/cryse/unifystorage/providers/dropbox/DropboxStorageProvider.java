package org.cryse.unifystorage.providers.dropbox;

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

    Retrofit mRetrofit;
    DropboxService mDropboxService;


    /*public DropboxStorageProvider(DbxClientV2 mDropboxClient) {
        this.mDropboxClient = mDropboxClient;
    }*/

    /*public DropboxStorageProvider(DropboxCredential credential, String clientIdentifier) {
        mDropboxCredential = credential;
        if (mDropboxCredential != null)
            mAuthenticationHeader = "Bearer " + mDropboxCredential.getAccessSecret();
        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https:" + DropboxService.SUBDOMAIN_API)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        dropboxService = retrofit.create(DropboxService.class);


        *//*if (mDropboxClient == null) {
            String userLocale = Locale.getDefault().toString();
            *//**//*DbxRequestConfig requestConfig = new DbxRequestConfig(
                    clientIdentifier,
                    userLocale,
                    OkHttpRequestor.Instance);

            mDropboxClient = new DbxClientV2(requestConfig, credential.getAccessToken(), DbxHost.Default);*//**//*
        }*//*
    }*/

    public DropboxStorageProvider(OkHttpClient okHttpClient, DropboxCredential credential, String clientIdentifier) {
        this.mDropboxCredential = credential;
        if (this.mDropboxCredential != null)
            this.mAuthenticationHeader = "Bearer " + this.mDropboxCredential.getAccessSecret();

        this.mRetrofit = new Retrofit.Builder()
                .baseUrl("https:" + DropboxService.SUBDOMAIN_API)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.mDropboxService = mRetrofit.create(DropboxService.class);
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
    public DirectoryInfo list(RemoteFile parent) throws StorageException {
        /*try {*/
        List<RemoteFile> list = new ArrayList<RemoteFile>();
        String parentPath = getPathString(parent);

        JsonObject requestData = new DropboxRequestDataBuilder()
                .listFolder(parentPath)
                .build();
        Call<JsonObject> call = mDropboxService.listFolders(mAuthenticationHeader, requestData);
        try {
            Response<JsonObject> response = call.execute();
            int responseCode = response.code();
            JsonObject responseObject = response.body();
            if (responseCode == 200) {
                if (responseObject.has("entries")) {
                    List<DropboxFile> fileMetas = gson.fromJson(responseObject.get("entries"), new TypeToken<List<DropboxFile>>() {
                    }.getType());
                    list.addAll(fileMetas);
                }
                while (responseObject.has("has_more") && responseObject.get("has_more").getAsBoolean() && responseObject.has("cursor")) {
                    JsonObject requestMoreData = new DropboxRequestDataBuilder()
                            .listFolderContinue(responseObject.get("cursor").getAsString())
                            .build();
                    Call<JsonObject> moreCall = mDropboxService.listFoldersContinue(mAuthenticationHeader, requestMoreData);
                    Response<JsonObject> moreResponse = moreCall.execute();
                    if (moreResponse.code() == 200) {
                        responseObject = response.body();
                        if (responseObject.has("entries")) {
                            List<DropboxFile> fileMetas = gson.fromJson(responseObject.get("entries"), new TypeToken<List<DropboxFile>>() {
                            }.getType());
                            list.addAll(fileMetas);
                        }
                    } else {
                        break;
                    }
                }
            } else {
                // Failure here
            }
            //String resultString = responseObject.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return DirectoryInfo.create(parent, list);
        /*} catch (DbxException ex) {
            throw new StorageException(ex);
        }*/
    }

    @Override
    public DropboxFile createDirectory(RemoteFile parent, String name) throws StorageException {
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
    public void copyFile(RemoteFile target, ProgressCallback callback, RemoteFile... files) {

    }

    @Override
    public void moveFile(RemoteFile target, ProgressCallback callback, RemoteFile... files) {

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
    public RemoteFileDownloader download(RemoteFile file) throws StorageException {
        JsonObject requestData = new DropboxRequestDataBuilder()
                .download(file.getPath())
                .build();
        Call<ResponseBody> call = mDropboxService.download(mAuthenticationHeader, requestData.toString());
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
