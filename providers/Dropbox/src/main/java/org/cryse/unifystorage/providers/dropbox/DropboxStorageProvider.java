package org.cryse.unifystorage.providers.dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxHost;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttpRequestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxFiles;

import org.cryse.unifystorage.AbstractStorageProvider;
import org.cryse.unifystorage.ConflictBehavior;
import org.cryse.unifystorage.FileUpdater;
import org.cryse.unifystorage.HashAlgorithm;
import org.cryse.unifystorage.StorageException;
import org.cryse.unifystorage.StorageUserInfo;
import org.cryse.unifystorage.utils.DirectoryPair;
import org.cryse.unifystorage.utils.Path;

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
    public DropboxFile getRootDirectory() throws StorageException {
        if (mRootFile == null) {
            mRootFile = new DropboxFile();
        }
        return mRootFile;
    }

    @Override
    public DirectoryPair<DropboxFile, List<DropboxFile>> list(DropboxFile parent) throws StorageException {
        try {
            List<DropboxFile> list = new ArrayList<DropboxFile>();
            String parentPath = parent.getPath().equalsIgnoreCase("/") ? "" : parent.getPath();
            DbxFiles.ListFolderResult listFolderResult = mDropboxClient.files.listFolder(parentPath);
            for(DbxFiles.Metadata metadata : listFolderResult.entries) {
                list.add(new DropboxFile(metadata));
            }
            return DirectoryPair.create(parent, list);
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
    public boolean deleteFile(DropboxFile file) throws StorageException {

        try {
            return null != mDropboxClient.files.delete(file.getPath());
        } catch (DbxException ex) {
            throw new StorageException(ex);
        }
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
    public boolean shouldRefreshCredential() {
        return false;
    }

    @Override
    public HashAlgorithm getHashAlgorithm() {
        return null;
    }
}
