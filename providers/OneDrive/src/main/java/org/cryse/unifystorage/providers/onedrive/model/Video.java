package org.cryse.unifystorage.providers.onedrive.model;

import com.google.gson.annotations.SerializedName;

public class Video {
    /**
     * The Bitrate.
     */
    @SerializedName("bitrate")
    public Integer bitrate;

    /**
     * The Duration.
     */
    @SerializedName("duration")
    public Long duration;

    /**
     * The Height.
     */
    @SerializedName("height")
    public Integer height;

    /**
     * The Width.
     */
    @SerializedName("width")
    public Integer width;
}
