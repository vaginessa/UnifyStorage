package org.cryse.unifystorage.explorer.utils.copy;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;

public class CopyTask {
    public StorageProviderInfo storageProviderInfo;
    public RemoteFile[] fileToCopy;

    public CopyTask(StorageProviderInfo storageProviderInfo, RemoteFile[] fileToCopy) {
        this.storageProviderInfo = storageProviderInfo;
        this.fileToCopy = fileToCopy;
    }
}
