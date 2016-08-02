package org.cryse.unifystorage.providers.onedrive.model;

import com.google.gson.annotations.SerializedName;

public class Location {
    /**
     * The Altitude.
     */
    @SerializedName("altitude")
    public Double altitude;

    /**
     * The Latitude.
     */
    @SerializedName("latitude")
    public Double latitude;

    /**
     * The Longitude.
     */
    @SerializedName("longitude")
    public Double longitude;
}
