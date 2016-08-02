package org.cryse.unifystorage.providers.onedrive.model;

import com.google.gson.annotations.SerializedName;

public class OpenWithApp {
    /**
     * The App.
     */
    @SerializedName("app")
    public Identity app;

    /**
     * The View Url.
     */
    @SerializedName("viewUrl")
    public String viewUrl;

    /**
     * The Edit Url.
     */
    @SerializedName("editUrl")
    public String editUrl;

    /**
     * The Post Parameters.
     */
    @SerializedName("postParameters")
    public String postParameters;
}
