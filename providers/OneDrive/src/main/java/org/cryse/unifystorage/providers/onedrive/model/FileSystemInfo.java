package org.cryse.unifystorage.providers.onedrive.model;

import com.google.gson.annotations.SerializedName;

public class FileSystemInfo {
    /**
     * The Created Date Time.
     */
    @SerializedName("createdDateTime")
    public java.util.Calendar createdDateTime;

    /**
     * The Last Modified Date Time.
     */
    @SerializedName("lastModifiedDateTime")
    public java.util.Calendar lastModifiedDateTime;
}
