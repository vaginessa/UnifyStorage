package org.cryse.unifystorage.providers.onedrive.model;

import com.google.gson.annotations.SerializedName;

public class File {
    /**
     * The Hashes.
     */
    @SerializedName("hashes")
    public Hashes hashes;

    /**
     * The Mime Type.
     */
    @SerializedName("mimeType")
    public String mimeType;
}
