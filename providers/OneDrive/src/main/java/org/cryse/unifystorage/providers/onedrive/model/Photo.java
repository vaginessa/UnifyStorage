package org.cryse.unifystorage.providers.onedrive.model;

import com.google.gson.annotations.SerializedName;

public class Photo {
    /**
     * The Camera Make.
     */
    @SerializedName("cameraMake")
    public String cameraMake;

    /**
     * The Camera Model.
     */
    @SerializedName("cameraModel")
    public String cameraModel;

    /**
     * The Exposure Denominator.
     */
    @SerializedName("exposureDenominator")
    public Double exposureDenominator;

    /**
     * The Exposure Numerator.
     */
    @SerializedName("exposureNumerator")
    public Double exposureNumerator;

    /**
     * The Focal Length.
     */
    @SerializedName("focalLength")
    public Double focalLength;

    /**
     * The FNumber.
     */
    @SerializedName("fNumber")
    public Double fNumber;

    /**
     * The Taken Date Time.
     */
    @SerializedName("takenDateTime")
    public java.util.Calendar takenDateTime;
}
