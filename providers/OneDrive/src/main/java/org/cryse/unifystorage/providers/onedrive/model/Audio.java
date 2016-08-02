package org.cryse.unifystorage.providers.onedrive.model;

import com.google.gson.annotations.SerializedName;

public class Audio {
    /**
     * The Album.
     */
    @SerializedName("album")
    public String album;

    /**
     * The Album Artist.
     */
    @SerializedName("albumArtist")
    public String albumArtist;

    /**
     * The Artist.
     */
    @SerializedName("artist")
    public String artist;

    /**
     * The Bitrate.
     */
    @SerializedName("bitrate")
    public Long bitrate;

    /**
     * The Composers.
     */
    @SerializedName("composers")
    public String composers;

    /**
     * The Copyright.
     */
    @SerializedName("copyright")
    public String copyright;

    /**
     * The Disc.
     */
    @SerializedName("disc")
    public Short disc;

    /**
     * The Disc Count.
     */
    @SerializedName("discCount")
    public Short discCount;

    /**
     * The Duration.
     */
    @SerializedName("duration")
    public Long duration;

    /**
     * The Genre.
     */
    @SerializedName("genre")
    public String genre;

    /**
     * The Has Drm.
     */
    @SerializedName("hasDrm")
    public Boolean hasDrm;

    /**
     * The Is Variable Bitrate.
     */
    @SerializedName("isVariableBitrate")
    public Boolean isVariableBitrate;

    /**
     * The Title.
     */
    @SerializedName("title")
    public String title;

    /**
     * The Track.
     */
    @SerializedName("track")
    public Integer track;

    /**
     * The Track Count.
     */
    @SerializedName("trackCount")
    public Integer trackCount;

    /**
     * The Year.
     */
    @SerializedName("year")
    public Integer year;
}
