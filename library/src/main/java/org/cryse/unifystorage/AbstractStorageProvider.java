package org.cryse.unifystorage;

import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.utils.DirectoryPair;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public abstract class AbstractStorageProvider<R extends RemoteFile, CR extends Credential> implements StorageProvider<R, CR> {
    public abstract R getRootDirectory() throws StorageException;

    public abstract DirectoryPair<R, List<R>> list(R parent) throws StorageException;

    public DirectoryPair<R, List<R>> list() throws StorageException {
        return list(getRootDirectory());
    }

    public abstract R createDirectory(R parent, String name) throws StorageException;

    public R createDirectory(String name) throws StorageException {
        return createDirectory(getRootDirectory(), name);
    }

    public abstract R createFile(R parent, String name, InputStream input, ConflictBehavior behavior) throws StorageException;

    public R createFile(R parent, String name, InputStream input) throws StorageException {
        return createFile(parent, name, input, getDefaultConflictBehavior());
    }

    public R createFile(String name, InputStream input) throws StorageException {
        return createFile(getRootDirectory(), name, input);
    }

    public R createFile(R parent, String name, LocalFile file, ConflictBehavior behavior) throws StorageException {
        try {
            InputStream inputStream = new FileInputStream(file.getFile());
            return createFile(parent, name, inputStream, behavior);
        } catch (FileNotFoundException fileNotFoundException) {
            throw new StorageException(fileNotFoundException);
        }
    }

    public R createFile(R parent, String name, LocalFile file) throws StorageException {
        return createFile(parent, name, file, getDefaultConflictBehavior());
    }

    public R createFile(String name, LocalFile file) throws StorageException {
        return createFile(getRootDirectory(), name, file);
    }

    public abstract boolean exists(R parent, String name) throws StorageException;

    public boolean exists(String name) throws StorageException {
        return exists(getRootDirectory(), name);
    }

    public abstract R getFile(R parent, String name) throws StorageException;

    public R getFile(String name) throws StorageException {
        return getFile(getRootDirectory(), name);
    }

    public abstract R getFileById(String id) throws StorageException;

    public abstract R updateFile(R remote, InputStream input, FileUpdater updater) throws StorageException;

    public R updateFile(R remote, InputStream input) throws StorageException {
        return updateFile(remote, input, null);
    }

    public R updateFile(R remote, LocalFile local, FileUpdater updater) throws StorageException {
        try {
            InputStream inputStream = new FileInputStream(local.getFile());
            return updateFile(remote, inputStream, updater);
        } catch (FileNotFoundException fileNotFoundException) {
            throw new StorageException(fileNotFoundException);
        }
    }

    public R updateFile(R remote, LocalFile local) throws StorageException {
        return updateFile(remote, local, null);
    }

    public abstract boolean deleteFile(R file) throws StorageException;

    public abstract R getFileDetail(R file) throws StorageException;

    public abstract R getFilePermission(R file) throws StorageException;

    public abstract R updateFilePermission(R file) throws StorageException;

    public abstract StorageUserInfo getUserInfo(boolean forceRefresh) throws StorageException;

    public StorageUserInfo getUserInfo() throws StorageException {
        return getUserInfo(false);
    }

    public abstract HashAlgorithm getHashAlgorithm();

    public ConflictBehavior getDefaultConflictBehavior() {
        return ConflictBehavior.FAIL;
    }
}
