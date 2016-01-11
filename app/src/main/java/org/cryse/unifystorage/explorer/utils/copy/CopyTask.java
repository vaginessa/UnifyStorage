package org.cryse.unifystorage.explorer.utils.copy;

import org.cryse.unifystorage.RemoteFile;

public class CopyTask<RF extends RemoteFile> {
    public int fromProviderId;
    public RF[] fileToCopy;
    public Class<RF> rfClass;

    public CopyTask(int fromProviderId, RF[] fileToCopy, Class<RF> rfClass) {
        this.fromProviderId = fromProviderId;
        this.fileToCopy = fileToCopy;
        this.rfClass = rfClass;
    }
}
