package org.cryse.unifystorage.explorer.service.task;

import org.cryse.unifystorage.explorer.model.StorageProviderInfo;

public abstract class RemoteTask extends Task {
    private StorageProviderInfo mProviderInfo;

    public RemoteTask(StorageProviderInfo providerInfo, boolean shouldQueue) {
        super(shouldQueue);
        this.mProviderInfo = providerInfo;
    }

    public StorageProviderInfo getProviderInfo() {
        return mProviderInfo;
    }

}
