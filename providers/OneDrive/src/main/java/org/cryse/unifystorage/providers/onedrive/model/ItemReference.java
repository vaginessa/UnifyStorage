package org.cryse.unifystorage.providers.onedrive.model;

import com.google.gson.annotations.SerializedName;

public class ItemReference {
    /**
     * The Drive Id.
     */
    @SerializedName("driveId")
    public String driveId;

    /**
     * The Id.
     */
    @SerializedName("id")
    public String id;

    /**
     * The Path.
     */
    @SerializedName("path")
    public String path;
}
