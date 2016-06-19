package org.cryse.unifystorage.explorer.service.task;

import android.content.Context;
import android.os.Handler;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.service.operation.DownloadOperation;
import org.cryse.unifystorage.explorer.service.operation.OnOperationListener;
import org.cryse.unifystorage.explorer.service.operation.Operation;
import org.cryse.unifystorage.explorer.service.operation.RemoteOperation;
import org.cryse.unifystorage.explorer.utils.HashUtils;

import java.util.Locale;

public class DownloadTask extends RemoteTask {
    public static final String OPT_NAME = DownloadOperation.OP_NAME;
    private RemoteFile mRemoteFile;
    private String mSavePath;

    public DownloadTask(StorageProviderInfo providerInfo, RemoteFile file, String savePath, boolean shouldQueue) {
        super(providerInfo, shouldQueue);
        mRemoteFile = file;
        mSavePath = savePath;
    }

    @Override
    public Operation getOperation(Context context, OnOperationListener listener, Handler listenerHandler) {
        return new DownloadOperation(
                generateToken(),
                new DownloadOperation.Params(
                        context,
                        getProviderInfo(),
                        getProviderInfo(),
                        mRemoteFile,
                        mSavePath
                ),
                listener,
                listenerHandler
        );
    }

    @Override
    public String generateToken() {
        return String.format(
                Locale.getDefault(),
                "%s-%d-%s-%s-%s",
                OPT_NAME,
                getProviderInfo().getStorageProviderId(),
                getProviderInfo().getCredential() == null ? "NULL" : getProviderInfo().getCredential().getAccountName(),
                HashUtils.md5(mRemoteFile.getId()),
                HashUtils.md5(mSavePath)
        );
    }
}
