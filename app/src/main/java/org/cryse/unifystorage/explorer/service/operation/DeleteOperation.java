package org.cryse.unifystorage.explorer.service.operation;

import android.content.Context;
import android.os.Handler;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.service.operation.base.OnOperationListener;
import org.cryse.unifystorage.explorer.service.operation.base.OperationState;
import org.cryse.unifystorage.explorer.service.operation.base.RemoteOperation;
import org.cryse.unifystorage.explorer.service.operation.base.RemoteOperationResult;

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
    public String getSummaryTitle(Context context) {
        return context.getString(R.string.operation_title_deleting_files);
    }

    @Override
    public String getSummaryContent(Context context) {
        String content = "";
        switch (getSummary().state) {
            case NEW:
            case PREPARING:
                content = context.getString(R.string.operation_content_preparing);
                break;
            case RUNNING:
                content = context.getString(
                        R.string.operation_content_deleting_files,
                        getSummary().itemIndex,
                        getSummary().itemCount
                );
                break;
        }
        return content;
    }

    @Override
    public String getSimpleSummaryContent(Context context) {
        return getSummaryContent(context);
    }

    @Override
    public double getSummaryProgress() {
        return getSummary().totalCountPercent * 100.0d;
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
