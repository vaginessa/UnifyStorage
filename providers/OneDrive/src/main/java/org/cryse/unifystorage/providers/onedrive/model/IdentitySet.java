package org.cryse.unifystorage.providers.onedrive.model;

import com.google.gson.annotations.SerializedName;

public class IdentitySet {
    /**
     * The Application.
     */
    @SerializedName("application")
    public Identity application;

    /**
     * The Device.
     */
    @SerializedName("device")
    public Identity device;

    /**
     * The User.
     */
    @SerializedName("user")
    public Identity user;
}
