package org.cryse.unifystorage.providers.dropbox;

import android.support.v4.util.Pair;
import android.util.Log;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxHost;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttpRequestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxFiles;
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
import org.cryse.unifystorage.providers.dropbox.model.DropboxRawFile;
import org.cryse.unifystorage.utils.DirectoryInfo;
import org.cryse.unifystorage.utils.Path;
import org.cryse.unifystorage.utils.ProgressCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DropboxStorageProvider extends AbstractStorageProvider<DropboxFile, DropboxCredential> {
    private DbxClientV2 mDropboxClient;
    private DropboxCredential mDropboxCredential;
    private DropboxFile mRootFile;
    private Gson gson = new Gson();
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.dropboxapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    DropboxService dropboxService = retrofit.create(DropboxService.class);


    public DropboxStorageProvider(DbxClientV2 mDropboxClient) {
        this.mDropboxClient = mDropboxClient;
    }

    public DropboxStorageProvider(DropboxCredential credential, String clientIdentifier) {
        mDropboxCredential = credential;
        if (mDropboxClient == null) {
            String userLocale = Locale.getDefault().toString();
            DbxRequestConfig requestConfig = new DbxRequestConfig(
                    clientIdentifier,
                    userLocale,
                    OkHttpRequestor.Instance);

            mDropboxClient = new DbxClientV2(requestConfig, credential.getAccessToken(), DbxHost.Default);
        }
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
        try {
            List<DropboxFile> list = new ArrayList<DropboxFile>();
            String parentPath = parent.getPath().equalsIgnoreCase("/") ? "" : parent.getPath();
            JsonObject requestData = new JsonObject();
            requestData.addProperty("path", parentPath);
            requestData.addProperty("recursive", false);
            requestData.addProperty("include_media_info", false);
            requestData.addProperty("include_deleted", false);
            Call<JsonObject> call = dropboxService.listFolders("Bearer " + mDropboxCredential.getAccessSecret(), requestData);
            try {
                Response<JsonObject> response = call.execute();
                int responseCode = response.code();
                JsonObject responseObject = response.body();
                if(responseCode == 200) {
                    if(responseObject.has("entries")) {
                        List<DropboxRawFile> fileMetas = gson.fromJson(responseObject.get("entries"), new TypeToken<List<DropboxRawFile>>(){}.getType());
                        for(DbxFiles.Metadata metadata : listFolderResult.entries) {
                            list.add(new DropboxFile(metadata));
                        }
                    }
                } else {
                    // Failure here
                }
                //String resultString = responseObject.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            DbxFiles.ListFolderResult listFolderResult = mDropboxClient.files.listFolder(parentPath);
            for(DbxFiles.Metadata metadata : listFolderResult.entries) {
                list.add(new DropboxFile(metadata));
            }
            return DirectoryInfo.create(parent, list);
        } catch (DbxException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public DropboxFile createDirectory(DropboxFile parent, String name) throws StorageException {
        try {
            return new DropboxFile(mDropboxClient.files.createFolder(Path.combine(parent.getPath(), name)));
        } catch (DbxException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public DropboxFile createFile(DropboxFile parent, String name, InputStream input, ConflictBehavior behavior) throws StorageException {
        return null;
    }

    @Override
    public boolean exists(DropboxFile parent, String name) throws StorageException {
        try {
            return null != mDropboxClient.files.getMetadata(Path.combine(parent.getPath(), name));
        } catch (DbxException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public DropboxFile getFile(DropboxFile parent, String name) throws StorageException {
        try {
            return new DropboxFile(mDropboxClient.files.getMetadata(Path.combine(parent.getPath(), name)));
        } catch (DbxException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public DropboxFile getFileById(String id) throws StorageException {
        try {
            return new DropboxFile(mDropboxClient.files.getMetadata(id));
        } catch (DbxException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public DropboxFile updateFile(DropboxFile remote, InputStream input, FileUpdater updater) throws StorageException {
        return null;
    }

    @Override
    public Pair<DropboxFile, Boolean> deleteFile(DropboxFile file) {
        try {
            return Pair.create(file, null != mDropboxClient.files.delete(file.getPath()));
        } catch (DbxException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public void copyFile(DropboxFile target, ProgressCallback callback, DropboxFile...files) {

    }

    @Override
    public void moveFile(DropboxFile target, ProgressCallback callback, DropboxFile...files) {

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
        try {
            DbxDownloader<DbxFiles.FileMetadata> downloader = mDropboxClient.files.downloadBuilder(file.getPath()).start();
            long time2 = System.currentTimeMillis();
            return new RemoteFileDownloader<>(new DropboxFile(downloader.result), downloader.body);
        } catch (DbxException e) {
            throw new StorageException(e);
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
}
