package org.cryse.unifystorage.explorer.service.task;

import android.content.Context;
import android.os.Handler;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.service.operation.CreateFolderOperation;
import org.cryse.unifystorage.explorer.service.operation.base.OnOperationListener;
import org.cryse.unifystorage.explorer.service.operation.base.Operation;
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
    public Operation getOperation(Context context, OnOperationListener listener, Handler listenerHandler) {

        return new CreateFolderOperation(
                generateToken(),
                new CreateFolderOperation.Params(
                        context,
                        getProviderInfo(),
                        mParentFile,
                        mName
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
                HashUtils.md5(mParentFile.getId()),
                mName
        );
    }
}
