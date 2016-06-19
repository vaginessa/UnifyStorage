package org.cryse.unifystorage.explorer.service.operation;

import android.content.Context;
import android.os.Handler;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;

public class UploadOperation extends RemoteOperation<UploadOperation.Params> {
    public static final String OP_NAME = "OP_UPLOAD";

    public UploadOperation(String token, Params params) {
        super(token, params);
    }

    public UploadOperation(String token, Params params, OnOperationListener listener, Handler listenerHandler) {
        super(token, params, listener, listenerHandler);
    }

    @Override
    protected RemoteOperationResult runOperation() {
        return null;
    }

    @Override
    protected void onBuildNotificationForState(OperationState state) {
        
    }

    @Override
    protected void onBuildNotificationForProgress(long currentRead, long currentSize, long itemIndex, long itemCount, long totalRead, long totalSize) {

    }

    @Override
    public String getOperationName() {
        return OP_NAME;
    }

    public static class Params extends RemoteOperation.Params {
        private RemoteFile mTarget;
        private RemoteFile[] mRemoteFiles;
        public Params(
                Context context,
                StorageProviderInfo sourceProviderInfo,
                StorageProviderInfo targetProviderInfo,
                RemoteFile targetParent,
                RemoteFile[] files
        ) {
            super(context, sourceProviderInfo, targetProviderInfo);
            mTarget = targetParent;
            mRemoteFiles = files;
        }

        public RemoteFile getTarget() {
            return mTarget;
        }

        public RemoteFile[] getRemoteFiles() {
            return mRemoteFiles;
        }
    }
}
