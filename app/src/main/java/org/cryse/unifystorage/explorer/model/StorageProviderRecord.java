package org.cryse.unifystorage.explorer.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class StorageProviderRecord extends RealmObject {
    public static final int PROVIDER_LOCAL_STORAGE = 1010;
    public static final int PROVIDER_ONE_DRIVE = 1011;
    public static final int PROVIDER_DROPBOX = 1012;
    public static final int PROVIDER_GOOGLE_DRIVE = 1013;

    private String displayName;
    private String userName;
    private int providerType;
    private String credentialData;
    private String extraData;
    private long sortKey;
    @PrimaryKey
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getProviderType() {
        return providerType;
    }

    public void setProviderType(int providerType) {
        this.providerType = providerType;
    }

    public String getCredentialData() {
        return credentialData;
    }

    public void setCredentialData(String credentialData) {
        this.credentialData = credentialData;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public long getSortKey() {
        return sortKey;
    }

    public void setSortKey(long sortKey) {
        this.sortKey = sortKey;
    }
}
