package org.cryse.unifystorage.explorer.service.task;

import android.content.Context;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.service.operation.CreateFolderOperation;
import org.cryse.unifystorage.explorer.service.operation.RemoteOperation;
import org.cryse.unifystorage.explorer.utils.HashUtils;

import java.util.Locale;

public class CreateFolderTask extends RemoteTask {
    public static final String OPT_NAME = CreateFolderOperation.OP_NAME;
    private RemoteFile mParentFile;
    private String mName;

    public CreateFolderTask(StorageProviderInfo providerInfo, RemoteFile parentFile, String name) {
        super(providerInfo, true);
        this.mParentFile = parentFile;
        this.mName = name;
    }

    @Override
    public CreateFolderOperation getOperation(Context context) {
        return new CreateFolderOperation(
                generateToken(),
                mParentFile,
                mName
        );
    }

    @Override
    protected String generateToken() {
        return String.format(
                Locale.getDefault(),
                "%s-%d-%s-%s-%s",
                OPT_NAME,
                getProviderInfo().getStorageProviderId(),
                getProviderInfo().getCredential() == null ? "NULL" : getProviderInfo().getCredential().getAccountName(),
                HashUtils.md5(mParentFile.getId()),
                mName
        );
    }
}
