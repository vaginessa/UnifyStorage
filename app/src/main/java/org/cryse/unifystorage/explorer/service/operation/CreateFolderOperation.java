package org.cryse.unifystorage.explorer.service.operation;

import android.content.Context;
import android.os.Handler;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.StorageException;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.service.operation.base.OnOperationListener;
import org.cryse.unifystorage.explorer.service.operation.base.OperationState;
import org.cryse.unifystorage.explorer.service.operation.base.RemoteOperation;
import org.cryse.unifystorage.explorer.service.operation.base.RemoteOperationResult;

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
    public String getSummaryTitle(Context context) {
        return context.getString(R.string.operation_title_creating_folder);
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
                        R.string.operation_content_creating_folder,
                        getParams().getFolderName(),
                        getParams().getParentFile().getPath()
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
        return -1;
    }

    @Override
    public String getSummaryCompletedTitle(Context context) {
        return "";
    }

    @Override
    public String getSummaryCompletedContent(Context context) {
        return "";
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
