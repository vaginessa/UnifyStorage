package org.cryse.unifystorage.providers.onedrive.model;

import com.google.gson.annotations.SerializedName;

public class OpenWithSet {
    /**
     * The Web.
     */
    @SerializedName("web")
    public OpenWithApp web;

    /**
     * The Web Embedded.
     */
    @SerializedName("webEmbedded")
    public OpenWithApp webEmbedded;
}
