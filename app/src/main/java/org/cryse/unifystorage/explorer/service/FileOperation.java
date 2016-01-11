package org.cryse.unifystorage.explorer.service;

import org.cryse.unifystorage.RemoteFile;

public class FileOperation<RF extends RemoteFile> {
    public enum FileOperationCode {
        COPY, MOVE, DELETE, RENAME, UPLOAD, DOWNLOAD, COMPRESS, UNCOMPRESS
    }

    private FileOperationCode mCode;
    private int mOperationId;
    private int mStorageProviderId;
    private RF mTarget;
    private RF[] mFiles;

    public FileOperation(FileOperationCode code, int operationId, int storageProviderId, RF target, RF...files) {
        this.mCode = code;
        this.mOperationId = operationId;
        this.mStorageProviderId = storageProviderId;
        this.mTarget = target;
        this.mFiles = files;
    }

    public FileOperationCode getCode() {
        return mCode;
    }

    public int getOperationId() {
        return mOperationId;
    }

    public int getStorageProviderId() {
        return mStorageProviderId;
    }

    public RF getTarget() {
        return mTarget;
    }

    public RF[] getFiles() {
        return mFiles;
    }
}
