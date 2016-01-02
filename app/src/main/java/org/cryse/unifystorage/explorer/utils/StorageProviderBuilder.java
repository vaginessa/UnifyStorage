package org.cryse.unifystorage.explorer.utils;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.credential.Credential;

public abstract class StorageProviderBuilder<
        RF extends RemoteFile,
        CR extends Credential,
        SP extends StorageProvider<RF, CR>
        > {
    public abstract SP buildStorageProvider(CR credential);
}
