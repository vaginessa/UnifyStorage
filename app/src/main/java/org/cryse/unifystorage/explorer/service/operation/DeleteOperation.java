package org.cryse.unifystorage.explorer.service.operation;

import android.content.Context;
import android.os.Handler;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;

import java.util.List;

public class DeleteOperation extends RemoteOperation<DeleteOperation.Params> {
    public static final String OP_NAME = "OP_DELETE";

    public DeleteOperation(String token, Params params) {
        super(token, params);
    }

    public DeleteOperation(String token, Params params, OnOperationListener listener, Handler listenerHandler) {
        super(token, params, listener, listenerHandler);
    }

    @Override
    protected RemoteOperationResult runOperation() {
        RemoteOperationResult result;
        StorageProvider storageProvider = getParams().getSourceStorageProvider();
        List<RemoteFile> filesToDelete = storageProvider.listRecursive(getParams().getRemoteFiles());
        boolean error = false;
        final int count = filesToDelete.size();
        for (int i = count - 1; i >= 0; i--) {
            RemoteFile file = filesToDelete.get(i);
            org.cryse.unifystorage.utils.OperationResult middleResult = storageProvider.deleteFile(file);
            if (!middleResult.isSuccess()) {
                error = true;
                break;
            } else {
                notifyOperationProgress(
                        0,
                        0,
                        count - i,
                        count,
                        0,
                        0
                );
            }
        }
        if(error) {
            result = new RemoteOperationResult(RemoteOperationResult.ResultCode.UNKNOWN_ERROR);
        } else {
            result = new RemoteOperationResult(RemoteOperationResult.ResultCode.OK);
        }
        return result;
    }

    @Override
    protected void onBuildNotificationForState(OperationState state) {
        switch (state) {
            case NEW:
            case PREPARING:
                getSummary().title.set("Deleting files...");
                getSummary().content.set("Preparing...");
                getSummary().simpleContent.set("Preparing...");
        }
    }

    @Override
    protected void onBuildNotificationForProgress(long currentRead, long currentSize, long itemIndex, long itemCount, long totalRead, long totalSize) {
        getSummary().displayPercent = getSummary().totalCountPercent;
        getSummary().content.set(String.format("Deleting %s of %s", itemIndex, itemCount));
        getSummary().simpleContent.set(String.format("Deleting %s of %s", itemIndex, itemCount));
    }

    @Override
    public String getOperationName() {
        return OP_NAME;
    }

    public static class Params extends RemoteOperation.Params {
        private RemoteFile[] mRemoteFiles;
        public Params(Context context, StorageProviderInfo sourceProviderInfo, StorageProviderInfo targetProviderInfo, RemoteFile[] remoteFiles) {
            super(context, sourceProviderInfo, targetProviderInfo);
            this.mRemoteFiles = remoteFiles;
        }

        public RemoteFile[] getRemoteFiles() {
            return mRemoteFiles;
        }
    }
}
