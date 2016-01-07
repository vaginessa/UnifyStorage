package org.cryse.unifystorage.providers.dropbox;

import android.support.v4.util.Pair;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxHost;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttpRequestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxFiles;

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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DropboxStorageProvider extends AbstractStorageProvider<DropboxFile, DropboxCredential> {
    private DbxClientV2 mDropboxClient;
    private DropboxFile mRootFile;

    public DropboxStorageProvider(DbxClientV2 mDropboxClient) {
        this.mDropboxClient = mDropboxClient;
    }

    public DropboxStorageProvider(DropboxCredential credential, String clientIdentifier) {
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
    public Pair<DropboxFile, Boolean> deleteFile(DropboxFile file) throws StorageException {
        try {
            return Pair.create(file, null != mDropboxClient.files.delete(file.getPath()));
        } catch (DbxException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public void copyFile(DropboxFile target, DropboxFile file, ProgressCallback callback) {

    }

    @Override
    public void moveFile(DropboxFile target, DropboxFile file, ProgressCallback callback) {

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
