package org.cryse.unifystorage.explorer.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class StorageUriRecord extends RealmObject {
    @PrimaryKey
    private String path;
    private String uriData;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUriData() {
        return uriData;
    }

    public void setUriData(String uriData) {
        this.uriData = uriData;
    }
}
