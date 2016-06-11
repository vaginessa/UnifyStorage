package org.cryse.unifystorage.explorer.service.task;

import android.content.Context;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.service.operation.DeleteOperation;
import org.cryse.unifystorage.explorer.service.operation.RemoteOperation;
import org.cryse.unifystorage.explorer.utils.HashUtils;

import java.util.Locale;

public class DeleteTask extends RemoteTask {
    public static final String OPT_NAME = DeleteOperation.OP_NAME;
    private RemoteFile[] mFiles;

    public DeleteTask(StorageProviderInfo providerInfo, boolean shouldQueue, RemoteFile[] files) {
        super(providerInfo, shouldQueue);
        this.mFiles = files;
    }

    @Override
    public RemoteOperation getOperation(Context context) {
        return new DeleteOperation(
                generateToken(),
                mFiles
        );
    }

    @Override
    protected String generateToken() {
        StringBuilder builder = new StringBuilder();
        for(RemoteFile remoteFile : mFiles) {
            builder.append(remoteFile.getId()).append("$\\/$");
        }
        return String.format(
                Locale.getDefault(),
                "%s-%d-%s-%d-%s",
                OPT_NAME,
                getProviderInfo().getStorageProviderId(),
                getProviderInfo().getCredential() == null ? "NULL" : getProviderInfo().getCredential().getAccountName(),
                mFiles.length,
                HashUtils.md5(builder.toString())
        );
    }
}
