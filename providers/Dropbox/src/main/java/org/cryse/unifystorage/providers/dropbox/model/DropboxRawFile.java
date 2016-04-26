package org.cryse.unifystorage.providers.dropbox.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class DropboxRawFile {
    @SerializedName(".tag")
    protected String type; // could be file or folder
    @SerializedName("name")
    protected String name;
    @SerializedName("path_lower")
    protected String pathLower;
    @SerializedName("path_display")
    protected String pathDisplay;
    @SerializedName("id")
    protected String id;
    @SerializedName("client_modified")
    protected Date clientModified;
    @SerializedName("server_modified")
    protected Date serverModified;
    @SerializedName("rev")
    protected String revision;
    @SerializedName("size")
    protected long size;
    @SerializedName("sharing_info")
    protected DropboxSharingInfo sharingInfo;
}
