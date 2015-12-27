package org.cryse.unifystorage;

import java.io.InputStream;
import java.util.List;

public interface StorageProvider<R extends RemoteFile> {
    R getRootDirectory() throws StorageException;

    List<R> list(R parent) throws StorageException;

    List<R> list() throws StorageException;

    R createDirectory(R parent, String name) throws StorageException;

    R createDirectory(String name) throws StorageException;

    R createFile(R parent, String name, InputStream input, ConflictBehavior behavior) throws StorageException;

    R createFile(R parent, String name, InputStream input) throws StorageException;

    R createFile(String name, InputStream input) throws StorageException;

    R createFile(R parent, String name, LocalFile file, ConflictBehavior behavior) throws StorageException;

    R createFile(R parent, String name, LocalFile file) throws StorageException;

    R createFile(String name, LocalFile file) throws StorageException;

    boolean exists(R parent, String name) throws StorageException;

    boolean exists(String name) throws StorageException;

    R getFile(R parent, String name) throws StorageException;

    R getFile(String name) throws StorageException;

    R getFileById(String id) throws StorageException;

    R updateFile(R remote, InputStream input, FileUpdater updater) throws StorageException;

    R updateFile(R remote, InputStream input) throws StorageException;

    R updateFile(R remote, LocalFile local, FileUpdater updater) throws StorageException;

    R updateFile(R remote, LocalFile local) throws StorageException;

    boolean deleteFile(R file) throws StorageException;

    R getFileDetail(R file) throws StorageException;

    R getFilePermission(R file) throws StorageException;

    R updateFilePermission(R file) throws StorageException;

    StorageUserInfo getUserInfo() throws StorageException;

    StorageUserInfo getUserInfo(boolean forceRefresh) throws StorageException;

    HashAlgorithm getHashAlgorithm() throws StorageException;
}
