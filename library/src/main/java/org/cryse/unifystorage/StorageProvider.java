package org.cryse.unifystorage;

import java.io.InputStream;
import java.util.List;

public interface StorageProvider {
    RemoteFile getRootDirectory() throws StorageException;

    List<RemoteFile> list(RemoteFile parent) throws StorageException;

    List<RemoteFile> list() throws StorageException;

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

    RemoteFile getFile(RemoteFile parent, String name) throws StorageException;

    RemoteFile getFile(String name) throws StorageException;

    RemoteFile getFileById(String id) throws StorageException;

    RemoteFile updateFile(RemoteFile remote, InputStream input, FileUpdater updater) throws StorageException;

    RemoteFile updateFile(RemoteFile remote, InputStream input) throws StorageException;

    RemoteFile updateFile(RemoteFile remote, LocalFile local, FileUpdater updater) throws StorageException;

    RemoteFile updateFile(RemoteFile remote, LocalFile local) throws StorageException;

    boolean deleteFile(RemoteFile file) throws StorageException;

    RemoteFile getFileDetail(RemoteFile file) throws StorageException;

    RemoteFile getFilePermission(RemoteFile file) throws StorageException;

    RemoteFile updateFilePermission(RemoteFile file) throws StorageException;

    StorageUserInfo getUserInfo() throws StorageException;

    StorageUserInfo getUserInfo(boolean forceRefresh) throws StorageException;

    HashAlgorithm getHashAlgorithm() throws StorageException;
}
