package org.cryse.unifystorage.explorer.service.task;

import android.content.Context;
import android.os.Handler;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.service.operation.base.OnOperationListener;
import org.cryse.unifystorage.explorer.service.operation.base.Operation;
import org.cryse.unifystorage.explorer.service.operation.copy.InStorageProviderCopyOperation;
import org.cryse.unifystorage.explorer.utils.HashUtils;

import java.util.Locale;

public class CopyTask extends RemoteTask {
    public static final String OP_NAME = "OP_COPY";
    private StorageProviderInfo mTargetStorageProviderInfo;
    private RemoteFile mTargetParent;
    private RemoteFile[] mFilesToCopy;
    public CopyTask(
            StorageProviderInfo providerInfo,
            StorageProviderInfo targetStorageProviderInfo,
            RemoteFile targetParent,
            RemoteFile[] filesToCopy,
            boolean shouldQueue
    ) {
        super(providerInfo, shouldQueue);
        mTargetStorageProviderInfo = targetStorageProviderInfo;
        mTargetParent = targetParent;
        mFilesToCopy = filesToCopy;
    }

    @Override
    public Operation getOperation(Context context, OnOperationListener listener, Handler listenerHandler) {
        if(mTargetStorageProviderInfo.getStorageProviderId() == mTargetStorageProviderInfo.getStorageProviderId()) {
            // In-StorageProvider Copy
            return new InStorageProviderCopyOperation(
                    generateToken(),
                    new InStorageProviderCopyOperation.Params(
                            context,
                            getProviderInfo(),
                            getProviderInfo(),
                            mTargetParent,
                            mFilesToCopy
                    ),
                    listener,
                    listenerHandler
            );
        } else if(mTargetStorageProviderInfo.isRemote() && getProviderInfo().isRemote()) {
            // Remote to Remote: Composite(Download, and Upload)
        } else if(!mTargetStorageProviderInfo.isRemote() && getProviderInfo().isRemote()) {
            // Remote to Local: Download
        } else if(mTargetStorageProviderInfo.isRemote() && !getProviderInfo().isRemote()) {
            // Local to Remote: Upload
        } else {
            // Local to Local
        }
        return null;
    }

    @Override
    public String generateToken() {
        StringBuilder builder = new StringBuilder();
        for(RemoteFile remoteFile : mFilesToCopy) {
            builder.append(remoteFile.getId()).append("$\\/$");
        }
        return String.format(
                Locale.getDefault(),
                "%s-%d-%d-%s-%s-%s-%d-%s",
                OP_NAME,
                getProviderInfo().getStorageProviderId(),
                mTargetStorageProviderInfo.getStorageProviderId(),
                getProviderInfo().getCredential() == null ? "NULL" : getProviderInfo().getCredential().getAccountName(),
                mTargetStorageProviderInfo.getCredential() == null ? "NULL" : getProviderInfo().getCredential().getAccountName(),
                HashUtils.md5(mTargetParent.getId()),
                mFilesToCopy.length,
                HashUtils.md5(builder.toString())
        );
    }
}
