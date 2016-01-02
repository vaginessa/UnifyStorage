package org.cryse.unifystorage;

import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.utils.DirectoryPair;

import java.io.InputStream;
import java.util.List;

public interface StorageProvider<RF extends RemoteFile, CR extends Credential> {
    RF getRootDirectory() throws StorageException;

    DirectoryPair<RF, List<RF>> list(RF parent) throws StorageException;

    DirectoryPair<RF, List<RF>> list() throws StorageException;

    RF createDirectory(RF parent, String name) throws StorageException;

    RF createDirectory(String name) throws StorageException;

    RF createFile(RF parent, String name, InputStream input, ConflictBehavior behavior) throws StorageException;

    RF createFile(RF parent, String name, InputStream input) throws StorageException;

    RF createFile(String name, InputStream input) throws StorageException;

    RF createFile(RF parent, String name, LocalFile file, ConflictBehavior behavior) throws StorageException;

    RF createFile(RF parent, String name, LocalFile file) throws StorageException;

    RF createFile(String name, LocalFile file) throws StorageException;

    boolean exists(RF parent, String name) throws StorageException;

    boolean exists(String name) throws StorageException;

    RF getFile(RF parent, String name) throws StorageException;

    RF getFile(String name) throws StorageException;

    RF getFileById(String id) throws StorageException;

    RF updateFile(RF remote, InputStream input, FileUpdater updater) throws StorageException;

    RF updateFile(RF remote, InputStream input) throws StorageException;

    RF updateFile(RF remote, LocalFile local, FileUpdater updater) throws StorageException;

    RF updateFile(RF remote, LocalFile local) throws StorageException;

    boolean deleteFile(RF file) throws StorageException;

    RF getFileDetail(RF file) throws StorageException;

    RF getFilePermission(RF file) throws StorageException;

    RF updateFilePermission(RF file) throws StorageException;

    StorageUserInfo getUserInfo() throws StorageException;

    StorageUserInfo getUserInfo(boolean forceRefresh) throws StorageException;

    CR getRefreshedCredential();

    boolean shouldRefreshCredential();

    HashAlgorithm getHashAlgorithm() throws StorageException;
}
