package org.cryse.unifystorage.explorer.service.operation;

import android.os.Handler;

import org.cryse.unifystorage.RemoteFile;

public class DeleteOperation extends RemoteOperation {
    public static final String OP_NAME = "OP_DELETE";
    private RemoteFile[] mRemoteFiles;

    public DeleteOperation(String operationToken, RemoteFile[] remoteFiles) {
        super(operationToken);
        mRemoteFiles = remoteFiles;
    }

    @Override
    public String getOperationName() {
        return OP_NAME;
    }

    @Override
    protected RemoteOperationResult run(RemoteOperationContext operationContext, final OnRemoteOperationListener listener, Handler listenerHandler) {
        if (listenerHandler != null && listener != null) {
            listenerHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onRemoteOperationStart(
                            DeleteOperation.this
                    );
                }
            });
        }
        boolean error = false;
        for(RemoteFile file : mRemoteFiles) {
            org.cryse.unifystorage.utils.OperationResult result = operationContext.getStorageProvider().deleteFile(file);
            if (!result.isSuccess()) {
                error = true;
                break;
            }
        }
        return new RemoteOperationResult(error ? RemoteOperationResult.ResultCode.UNKNOWN_ERROR : RemoteOperationResult.ResultCode.OK);
    }
}
