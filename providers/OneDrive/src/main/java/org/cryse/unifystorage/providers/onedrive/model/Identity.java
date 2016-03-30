package org.cryse.unifystorage.providers.onedrive.model;

import com.google.gson.annotations.SerializedName;

public class Identity {
    /**
     * The Display Name.
     */
    @SerializedName("displayName")
    public String displayName;

    /**
     * The Id.
     */
    @SerializedName("id")
    public String id;
}
