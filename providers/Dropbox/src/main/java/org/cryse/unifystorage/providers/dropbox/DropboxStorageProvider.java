package org.cryse.unifystorage.providers.dropbox;

import android.support.v4.util.Pair;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.cryse.unifystorage.AbstractStorageProvider;
import org.cryse.unifystorage.ConflictBehavior;
import org.cryse.unifystorage.RemoteFileDownloader;
import org.cryse.unifystorage.FileUpdater;
import org.cryse.unifystorage.HashAlgorithm;
import org.cryse.unifystorage.StorageException;
import org.cryse.unifystorage.StorageUserInfo;
import org.cryse.unifystorage.utils.DirectoryInfo;
import org.cryse.unifystorage.utils.Path;
import org.cryse.unifystorage.utils.ProgressCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DropboxStorageProvider extends AbstractStorageProvider<DropboxFile, DropboxCredential> {
    // private DbxClientV2 mDropboxClient;
    private DropboxCredential mDropboxCredential;
    private DropboxFile mRootFile;
    private Gson gson = new Gson();
    private String mAuthenticationHeader = "";


    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https:" + DropboxService.SUBDOMAIN_API)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    DropboxService dropboxService = retrofit.create(DropboxService.class);


    /*public DropboxStorageProvider(DbxClientV2 mDropboxClient) {
        this.mDropboxClient = mDropboxClient;
    }*/

    public DropboxStorageProvider(DropboxCredential credential, String clientIdentifier) {
        mDropboxCredential = credential;
        if (mDropboxCredential != null)
            mAuthenticationHeader = "Bearer " + mDropboxCredential.getAccessSecret();
        /*if (mDropboxClient == null) {
            String userLocale = Locale.getDefault().toString();
            *//*DbxRequestConfig requestConfig = new DbxRequestConfig(
                    clientIdentifier,
                    userLocale,
                    OkHttpRequestor.Instance);

            mDropboxClient = new DbxClientV2(requestConfig, credential.getAccessToken(), DbxHost.Default);*//*
        }*/
    }

    @Override
    public String getStorageProviderName() {
        return DropboxConst.NAME_STORAGE_PROVIDER;
    }

    @Override
    public DropboxFile getRootDirectory() throws StorageException {
        if (mRootFile == null) {
            mRootFile = new DropboxFile();
        }
        return mRootFile;
    }

    @Override
    public DirectoryInfo<DropboxFile, List<DropboxFile>> list(DropboxFile parent) throws StorageException {
        /*try {*/
        List<DropboxFile> list = new ArrayList<DropboxFile>();
        String parentPath = getPathString(parent);

        JsonObject requestData = new DropboxRequestDataBuilder()
                .listFolder(parentPath)
                .build();
        Call<JsonObject> call = dropboxService.listFolders(mAuthenticationHeader, requestData);
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
                    Call<JsonObject> moreCall = dropboxService.listFoldersContinue(mAuthenticationHeader, requestMoreData);
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
    public DropboxFile createDirectory(DropboxFile parent, String name) throws StorageException {
        DropboxFile fileMetaData = null;
        JsonObject requestData = new DropboxRequestDataBuilder()
                .createFolder(Path.combine(parent.getPath(), name))
                .build();
        Call<JsonObject> call = dropboxService.createFolder(mAuthenticationHeader, requestData);
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
    public DropboxFile createFile(DropboxFile parent, String name, InputStream input, ConflictBehavior behavior) throws StorageException {
        return null;
    }

    @Override
    public boolean exists(DropboxFile parent, String name) throws StorageException {
        DropboxFile fileMetaData = null;
        JsonObject requestData = new DropboxRequestDataBuilder()
                .getMetaData(Path.combine(parent.getPath(), name))
                .build();
        Call<JsonObject> call = dropboxService.getMetaData(mAuthenticationHeader, requestData);
        try {
            Response<JsonObject> response = call.execute();
            int responseCode = response.code();
            JsonObject responseObject = response.body();
            if (responseCode == 200) {
                fileMetaData = gson.fromJson(responseObject, DropboxFile.class);
                return true;
            } else {
                // Failure here
                Log.e("aaaa", responseObject.toString());
            }
            //String resultString = responseObject.toString();
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
        return false;
    }

    @Override
    public DropboxFile getFile(DropboxFile parent, String name) throws StorageException {
        DropboxFile fileMetaData = null;
        JsonObject requestData = new DropboxRequestDataBuilder()
                .getMetaData(Path.combine(parent.getPath(), name))
                .build();
        Call<JsonObject> call = dropboxService.getMetaData(mAuthenticationHeader, requestData);
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
        Call<JsonObject> call = dropboxService.getMetaData(mAuthenticationHeader, requestData);
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
    public DropboxFile updateFile(DropboxFile remote, InputStream input, FileUpdater updater) throws StorageException {
        return null;
    }

    @Override
    public Pair<DropboxFile, Boolean> deleteFile(DropboxFile file) {
        DropboxFile fileMetaData = null;
        JsonObject requestData = new DropboxRequestDataBuilder()
                .delete(file.getPath())
                .build();
        Call<JsonObject> call = dropboxService.delete(mAuthenticationHeader, requestData);
        try {
            Response<JsonObject> response = call.execute();
            int responseCode = response.code();
            JsonObject responseObject = response.body();
            if (responseCode == 200) {
                fileMetaData = gson.fromJson(responseObject, DropboxFile.class);
            } else {
                // Failure here
                return Pair.create(file, false);
            }
            //String resultString = responseObject.toString();
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
        return Pair.create(file, null != fileMetaData);
    }

    @Override
    public void copyFile(DropboxFile target, ProgressCallback callback, DropboxFile... files) {

    }

    @Override
    public void moveFile(DropboxFile target, ProgressCallback callback, DropboxFile... files) {

    }

    @Override
    public DropboxFile getFileDetail(DropboxFile file) throws StorageException {
        return null;
    }

    @Override
    public DropboxFile getFilePermission(DropboxFile file) throws StorageException {
        return null;
    }

    @Override
    public DropboxFile updateFilePermission(DropboxFile file) throws StorageException {
        return null;
    }

    @Override
    public StorageUserInfo getUserInfo(boolean forceRefresh) throws StorageException {
        return null;
    }

    @Override
    public DropboxCredential getRefreshedCredential() {
        return null;
    }

    @Override
    public RemoteFileDownloader<DropboxFile> download(DropboxFile file) throws StorageException {
        JsonObject requestData = new DropboxRequestDataBuilder()
                .download(file.getPath())
                .build();
        Log.e("abababab", requestData.toString());
        Call<ResponseBody> call = dropboxService.download(mAuthenticationHeader, requestData.toString());
        try {
            Response<ResponseBody> response = call.execute();
            int responseCode = response.code();
            ResponseBody responseBody = response.body();
            if (responseCode == 200) {
                return new RemoteFileDownloader<>(file, responseBody.byteStream());
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
    public boolean shouldRefreshCredential() {
        return false;
    }

    @Override
    public HashAlgorithm getHashAlgorithm() {
        return null;
    }

    private static String getPathString(DropboxFile file) {
        if(file == null) return "";
        else if(file.getPath() == null) return "";
        else return file.getPath().equalsIgnoreCase("/") ? "" : file.getPath();
    }
}
