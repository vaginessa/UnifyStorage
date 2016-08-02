package org.cryse.unifystorage.explorer.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class StorageProviderRecord extends RealmObject {
//    public static final int PROVIDER_LOCAL_STORAGE = 1000;
//    public static final int PROVIDER_REMOTE = 10000;
//    public static final int PROVIDER_ONE_DRIVE = PROVIDER_REMOTE + 1;
//    public static final int PROVIDER_DROPBOX = PROVIDER_REMOTE + 2;
//    public static final int PROVIDER_GOOGLE_DRIVE = PROVIDER_REMOTE + 3;

    @PrimaryKey
    private int id;
    private int providerType;
    private String uuid;
    private String displayName;
    private String userName;
    private String credentialData;
    private String extraData;
    private long sortKey;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProviderType() {
        return providerType;
    }

    public void setProviderType(int providerType) {
        this.providerType = providerType;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
