package org.cryse.unifystorage.explorer.utils.copy;

import org.cryse.unifystorage.RemoteFile;

public class CopyTask {
    public int fromProviderId;
    public RemoteFile[] fileToCopy;

    public CopyTask(int fromProviderId, RemoteFile[] fileToCopy) {
        this.fromProviderId = fromProviderId;
        this.fileToCopy = fileToCopy;
    }
}
