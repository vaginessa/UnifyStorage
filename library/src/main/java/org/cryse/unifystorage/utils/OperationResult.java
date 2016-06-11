package org.cryse.unifystorage.utils;

import org.cryse.unifystorage.RemoteFile;

public class OperationResult {
    private RemoteFile mFile;
    private boolean mSuccess;
    public OperationResult(RemoteFile file, Boolean success) {
        mFile = file;
        mSuccess = success;
    }

    public static OperationResult create(RemoteFile file, Boolean success) {
        return new OperationResult(file, success);
    }

    public RemoteFile getFile() {
        return mFile;
    }

    public boolean isSuccess() {
        return mSuccess;
    }
}

