package org.cryse.unifystorage.explorer.model;

import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.providers.dropbox.DropboxCredential;
import org.cryse.unifystorage.providers.onedrive.OneDriveCredential;

public class StorageProviderInfo {
    private int mStorageProviderId;
    private StorageProviderType mStorageProviderType;
    private String mDisplayName;
    private Credential mCredential;
    private String[] mExtras;

    public StorageProviderInfo(
            int storageProviderId,
            StorageProviderType storageProviderType,
            String displayName,
            Credential credential,
            String[] extras
    ) {
        this.mStorageProviderId = storageProviderId;
        this.mStorageProviderType = storageProviderType;
        this.mDisplayName = displayName;
        this.mCredential = credential;
        this.mExtras = extras;
    }

    public static StorageProviderInfo fromRecord(StorageProviderRecord record) {
        Credential credential;
        StorageProviderType type = StorageProviderType.fromInt(record.getProviderType());
        switch (type) {
            case DROPBOX:
                credential = new DropboxCredential(record.getCredentialData());
                break;
            case ONE_DRIVE:
                credential = new OneDriveCredential(record.getCredentialData());
                break;
            case GOOGLE_DRIVE:
                credential = null;
                break;
            default:
                credential = null;
        }
        return new StorageProviderInfo(
                record.getId(),
                StorageProviderType.fromInt(record.getProviderType()),
                record.getDisplayName(),
                credential,
                new String[]{record.getExtraData()}
        );
    }

    public int getStorageProviderId() {
        return mStorageProviderId;
    }

    public void setStorageProviderId(int storageProviderId) {
        this.mStorageProviderId = storageProviderId;
    }

    public StorageProviderType getStorageProviderType() {
        return mStorageProviderType;
    }

    public void setStorageProviderType(StorageProviderType storageProviderType) {
        this.mStorageProviderType = storageProviderType;
    }

    public String getDisplayName() {
        return mDisplayName;
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

    public boolean isRemote() {
        return mStorageProviderType.isRemote();
    }
}
