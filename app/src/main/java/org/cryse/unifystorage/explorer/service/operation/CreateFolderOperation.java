package org.cryse.unifystorage.explorer.service.operation;

import android.os.Handler;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.StorageException;

public class CreateFolderOperation extends RemoteOperation {
    public static final String OP_NAME = "OP_CREATE_FOLDER";
    private RemoteFile mParent;
    private String mFolderName;

    public CreateFolderOperation(String operationToken, RemoteFile parent, String folderName) {
        super(operationToken);
        this.mParent = parent;
        this.mFolderName = folderName;
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
                            CreateFolderOperation.this
                    );
                }
            });
        }
        try {
            RemoteFile remoteFile = operationContext.getStorageProvider().createDirectory(mParent, mFolderName);
            return new RemoteOperationResult(remoteFile);
        } catch (StorageException ex) {
            return new RemoteOperationResult(ex);
        }
    }
}
