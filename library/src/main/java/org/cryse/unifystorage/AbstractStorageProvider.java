package org.cryse.unifystorage;

import org.cryse.unifystorage.utils.DirectoryInfo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public abstract class AbstractStorageProvider implements StorageProvider {
    protected OnTokenRefreshListener mOnTokenRefreshListener = null;

    public abstract RemoteFile getRootDirectory() throws StorageException;

    public abstract DirectoryInfo list(RemoteFile parent) throws StorageException;

    public DirectoryInfo list() throws StorageException {
        return list(getRootDirectory());
    }

    public abstract RemoteFile createDirectory(RemoteFile parent, String name) throws StorageException;

    public RemoteFile createDirectory(String name) throws StorageException {
        return createDirectory(getRootDirectory(), name);
    }

    public abstract RemoteFile createFile(RemoteFile parent, String name, InputStream input, ConflictBehavior behavior) throws StorageException;

    public RemoteFile createFile(RemoteFile parent, String name, InputStream input) throws StorageException {
        return createFile(parent, name, input, getDefaultConflictBehavior());
    }

    public RemoteFile createFile(String name, InputStream input) throws StorageException {
        return createFile(getRootDirectory(), name, input);
    }

    public RemoteFile createFile(RemoteFile parent, String name, LocalFile file, ConflictBehavior behavior) throws StorageException {
        try {
            InputStream inputStream = new FileInputStream(file.getFile());
            return createFile(parent, name, inputStream, behavior);
        } catch (FileNotFoundException fileNotFoundException) {
            throw new StorageException(fileNotFoundException);
        }
    }

    public RemoteFile createFile(RemoteFile parent, String name, LocalFile file) throws StorageException {
        return createFile(parent, name, file, getDefaultConflictBehavior());
    }

    public RemoteFile createFile(String name, LocalFile file) throws StorageException {
        return createFile(getRootDirectory(), name, file);
    }

    public abstract boolean exists(RemoteFile parent, String name) throws StorageException;

    public boolean exists(String name) throws StorageException {
        return exists(getRootDirectory(), name);
    }

    public abstract RemoteFile getFile(RemoteFile parent, String name) throws StorageException;

    public RemoteFile getFile(String name) throws StorageException {
        return getFile(getRootDirectory(), name);
    }

    public abstract RemoteFile getFileById(String id) throws StorageException;

    public abstract RemoteFile updateFile(RemoteFile remote, InputStream input, FileUpdater updater) throws StorageException;

    public RemoteFile updateFile(RemoteFile remote, InputStream input) throws StorageException {
        return updateFile(remote, input, null);
    }

    public RemoteFile updateFile(RemoteFile remote, LocalFile local, FileUpdater updater) throws StorageException {
        try {
            InputStream inputStream = new FileInputStream(local.getFile());
            return updateFile(remote, inputStream, updater);
        } catch (FileNotFoundException fileNotFoundException) {
            throw new StorageException(fileNotFoundException);
        }
    }

    public RemoteFile updateFile(RemoteFile remote, LocalFile local) throws StorageException {
        return updateFile(remote, local, null);
    }

    public abstract RemoteFile getFileDetail(RemoteFile file) throws StorageException;

    public abstract RemoteFile getFilePermission(RemoteFile file) throws StorageException;

    public abstract RemoteFile updateFilePermission(RemoteFile file) throws StorageException;

    public abstract StorageUserInfo getUserInfo(boolean forceRefresh) throws StorageException;

    public StorageUserInfo getUserInfo() throws StorageException {
        return getUserInfo(false);
    }

    public abstract HashAlgorithm getHashAlgorithm();

    public ConflictBehavior getDefaultConflictBehavior() {
        return ConflictBehavior.FAIL;
    }

    public void setOnTokenRefreshListener(OnTokenRefreshListener listener) {
        this.mOnTokenRefreshListener = listener;
    }
}
