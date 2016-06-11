package org.cryse.unifystorage.explorer.service.task;

import android.content.Context;

import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.service.operation.RemoteOperation;

public abstract class RemoteTask extends Task {
    private StorageProviderInfo mProviderInfo;

    public RemoteTask(StorageProviderInfo providerInfo, boolean shouldQueue) {
        super(shouldQueue);
        this.mProviderInfo = providerInfo;
    }

    public abstract RemoteOperation getOperation(Context context);

    public RemoteOperation.RemoteOperationContext getOperationContext(Context context) {
        return new RemoteOperation.RemoteOperationContext(context, mProviderInfo);
    }

    public StorageProviderInfo getProviderInfo() {
        return mProviderInfo;
    }

}
