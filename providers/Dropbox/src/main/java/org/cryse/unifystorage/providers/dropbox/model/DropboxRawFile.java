package org.cryse.unifystorage.providers.dropbox.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class DropboxRawFile {
    @SerializedName(".tag")
    public String type; // could be file or folder
    @SerializedName("name")
    public String name;
    @SerializedName("path_lower")
    public String pathLower;
    @SerializedName("path_display")
    public String pathDisplay;
    @SerializedName("id")
    public String id;
    @SerializedName("client_modified")
    public Date clientModified;
    @SerializedName("server_modified")
    public Date serverModified;
    @SerializedName("rev")
    public String revision;
    @SerializedName("size")
    public long size;
    @SerializedName("sharing_info")
    public DropboxSharingInfo sharingInfo;
}
