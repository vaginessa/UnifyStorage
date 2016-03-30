package org.cryse.unifystorage.providers.onedrive.model;

import com.google.gson.annotations.SerializedName;

public class Hashes {
    /**
     * The Crc32Hash.
     */
    @SerializedName("crc32Hash")
    public String crc32Hash;

    /**
     * The Sha1Hash.
     */
    @SerializedName("sha1Hash")
    public String sha1Hash;
}
