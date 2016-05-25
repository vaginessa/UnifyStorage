package org.cryse.unifystorage.explorer.model;

import org.cryse.unifystorage.credential.Credential;

public class StorageProviderInfo {
    private int mStorageProviderId;
    private Credential mCredential;
    private String[] mExtras;

    public StorageProviderInfo(int mStorageProviderId, Credential mCredential, String[] mExtras) {
        this.mStorageProviderId = mStorageProviderId;
        this.mCredential = mCredential;
        this.mExtras = mExtras;
    }

    public int getStorageProviderId() {
        return mStorageProviderId;
    }

    public void setStorageProviderId(int storageProviderId) {
        this.mStorageProviderId = storageProviderId;
    }

    public Credential getCredential() {
        return mCredential;
    }

    public void setCredential(Credential credential) {
        this.mCredential = credential;
    }

    public String[] getExtras() {
        return mExtras;
    }

    public void setExtras(String[] extras) {
        this.mExtras = extras;
    }
}
