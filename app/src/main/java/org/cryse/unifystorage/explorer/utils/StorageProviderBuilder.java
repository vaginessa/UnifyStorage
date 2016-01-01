package org.cryse.unifystorage.explorer.utils;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.credential.Credential;

public abstract class StorageProviderBuilder<
        RF extends RemoteFile,
        SP extends StorageProvider<RF>,
        CR extends Credential
        > {
    public abstract SP buildStorageProvider(CR credential);
}
