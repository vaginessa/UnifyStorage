package org.cryse.unifystorage;

import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.utils.OperationResult;
import org.cryse.unifystorage.utils.DirectoryInfo;
import org.cryse.unifystorage.utils.ProgressCallback;

import java.io.InputStream;
import java.util.List;

import okhttp3.Request;

public interface StorageProvider {
    boolean isRemote();

    String getStorageProviderName();

    RemoteFile getRootDirectory() throws StorageException;

    DirectoryInfo list(DirectoryInfo directoryInfo) throws StorageException;

    DirectoryInfo list() throws StorageException;

    DirectoryInfo list(String path) throws StorageException;

    List<RemoteFile> listRecursive(RemoteFile[] remoteFiles) throws StorageException;

    RemoteFile createDirectory(RemoteFile parent, String name) throws StorageException;

    RemoteFile createDirectory(String name) throws StorageException;

    RemoteFile createFile(RemoteFile parent, String name, InputStream input, ConflictBehavior behavior) throws StorageException;

    RemoteFile createFile(RemoteFile parent, String name, InputStream input) throws StorageException;

    RemoteFile createFile(String name, InputStream input) throws StorageException;

    RemoteFile createFile(RemoteFile parent, String name, LocalFile file, ConflictBehavior behavior) throws StorageException;

    RemoteFile createFile(RemoteFile parent, String name, LocalFile file) throws StorageException;

    RemoteFile createFile(String name, LocalFile file) throws StorageException;

    boolean exists(RemoteFile parent, String name) throws StorageException;

    boolean exists(String name) throws StorageException;

    RemoteFile getFile(String path) throws StorageException;

    RemoteFile getFile(RemoteFile parent, String name) throws StorageException;

    RemoteFile getFileById(String id) throws StorageException;

    RemoteFile updateFile(RemoteFile remote, InputStream input, FileUpdater updater) throws StorageException;

    RemoteFile updateFile(RemoteFile remote, InputStream input) throws StorageException;

    RemoteFile updateFile(RemoteFile remote, LocalFile local, FileUpdater updater) throws StorageException;

    RemoteFile updateFile(RemoteFile remote, LocalFile local) throws StorageException;

    OperationResult deleteFile(RemoteFile file);

    void copyFile(RemoteFile targetParent, RemoteFile file) throws StorageException;

    void copyFile(RemoteFile targetParent, RemoteFile file, final ProgressCallback callback) throws StorageException;

    void moveFile(RemoteFile targetParent, RemoteFile file) throws StorageException;

    void moveFile(RemoteFile targetParent, RemoteFile file, final ProgressCallback callback) throws StorageException;

    RemoteFile getFileDetail(RemoteFile file) throws StorageException;

    RemoteFile getFilePermission(RemoteFile file) throws StorageException;

    RemoteFile updateFilePermission(RemoteFile file) throws StorageException;

    StorageUserInfo getUserInfo() throws StorageException;

    StorageUserInfo getUserInfo(boolean forceRefresh) throws StorageException;

    Request download(RemoteFile file) throws StorageException;

    HashAlgorithm getHashAlgorithm() throws StorageException;

    void setOnTokenRefreshListener(OnTokenRefreshListener listener);

    interface OnTokenRefreshListener {
        void onTokenRefresh(Credential refreshedCredential);
    }
}
