package org.cryse.unifystorage.explorer.model;

public enum StorageProviderType {
    INTERNAL_STORAGE(10000),
    LOCAL_STORAGE(10001),
    DROPBOX(20000 + 1),
    ONE_DRIVE(20000 + 2),
    GOOGLE_DRIVE(20000 + 3);

    private int value;

    StorageProviderType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean isRemote() {
        return value > 20000;
    }

    public static StorageProviderType fromInt(int i) {
        for (StorageProviderType b : StorageProviderType.values()) {
            if (b.getValue() == i) { return b; }
        }
        return null;
    }
}
