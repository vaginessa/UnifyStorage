package org.cryse.unifystorage.explorer.service;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.credential.Credential;

public class FileOperation {
    public enum FileOperationCode {
        COPY, MOVE, DELETE, RENAME, UPLOAD, DOWNLOAD, COMPRESS, UNCOMPRESS
    }

    private FileOperationCode mCode;
    private int mOperationId;
    private StorageProviderInfo mStorageProviderInfo;
    private RemoteFile mTarget;
    private RemoteFile[] mFiles;

    public FileOperation(FileOperationCode code, int operationId, StorageProviderInfo storageProviderInfo, RemoteFile target, RemoteFile...files) {
        this.mCode = code;
        this.mOperationId = operationId;
        this.mStorageProviderInfo = storageProviderInfo;
        this.mTarget = target;
        this.mFiles = files;
    }

    public FileOperationCode getCode() {
        return mCode;
    }

    public int getOperationId() {
        return mOperationId;
    }

    public StorageProviderInfo getStorageProviderInfo() {
        return mStorageProviderInfo;
    }

    public RemoteFile getTarget() {
        return mTarget;
    }

    public RemoteFile[] getFiles() {
        return mFiles;
    }

    public static class StorageProviderInfo {
        public int id;
        public Credential credential;
        public String[] extras;

        public StorageProviderInfo(int id, Credential credential, String[] extras) {
            this.id = id;
            this.credential = credential;
            this.extras = extras;
        }
    }
}
