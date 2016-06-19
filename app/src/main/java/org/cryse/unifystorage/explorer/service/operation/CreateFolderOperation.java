package org.cryse.unifystorage.explorer.service.operation;

import android.content.Context;
import android.os.Handler;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.StorageException;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;

public class CreateFolderOperation extends RemoteOperation<CreateFolderOperation.Params> {
    public static final String OP_NAME = "OP_CREATE_FOLDER";

    public CreateFolderOperation(String token, Params params) {
        super(token, params);
    }

    public CreateFolderOperation(String token, Params params, OnOperationListener listener, Handler listenerHandler) {
        super(token, params, listener, listenerHandler);
    }

    @Override
    public String getOperationName() {
        return OP_NAME;
    }

    @Override
    protected RemoteOperationResult runOperation() {
        RemoteOperationResult result;
        try {
            RemoteFile remoteFile = getParams().getSourceStorageProvider().createDirectory(
                    getParams().getParentFile(),
                    getParams().getFolderName()
            );
            result = new RemoteOperationResult(remoteFile);
        } catch (StorageException ex) {
            result = new RemoteOperationResult(ex);
        }
        return result;
    }

    @Override
    protected void onBuildNotificationForState(OperationState state) {

    }

    @Override
    protected void onBuildNotificationForProgress(long currentRead, long currentSize, long itemIndex, long itemCount, long totalRead, long totalSize) {

    }

    public static class Params extends RemoteOperation.Params {
        private RemoteFile mParentFile;
        private String mFolderName;

        public Params(Context context, StorageProviderInfo providerInfo, RemoteFile parentFile, String name) {
            super(context, providerInfo, providerInfo);
            this.mParentFile = parentFile;
            this.mFolderName = name;
        }

        public RemoteFile getParentFile() {
            return mParentFile;
        }

        public String getFolderName() {
            return mFolderName;
        }
    }
}
