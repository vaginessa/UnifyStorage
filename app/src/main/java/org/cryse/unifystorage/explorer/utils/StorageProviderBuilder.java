package org.cryse.unifystorage.explorer.utils;

import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.credential.Credential;

public abstract class StorageProviderBuilder {
    public abstract StorageProvider buildStorageProvider(Credential credential);
}
