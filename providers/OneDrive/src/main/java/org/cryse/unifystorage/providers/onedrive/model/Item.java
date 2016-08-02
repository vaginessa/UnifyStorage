package org.cryse.unifystorage.providers.onedrive.model;

import com.google.gson.annotations.SerializedName;

public class Item {
    /**
     * The Created By.
     */
    @SerializedName("createdBy")
    public IdentitySet createdBy;

    /**
     * The Created Date Time.
     */
    @SerializedName("createdDateTime")
    public java.util.Calendar createdDateTime;

    /**
     * The CTag.
     */
    @SerializedName("cTag")
    public String cTag;

    /**
     * The Description.
     */
    @SerializedName("description")
    public String description;

    /**
     * The ETag.
     */
    @SerializedName("eTag")
    public String eTag;

    /**
     * The Id.
     */
    @SerializedName("id")
    public String id;

    /**
     * The Last Modified By.
     */
    @SerializedName("lastModifiedBy")
    public IdentitySet lastModifiedBy;

    /**
     * The Last Modified Date Time.
     */
    @SerializedName("lastModifiedDateTime")
    public java.util.Calendar lastModifiedDateTime;

    /**
     * The Name.
     */
    @SerializedName("name")
    public String name;

    /**
     * The Parent Reference.
     */
    @SerializedName("parentReference")
    public ItemReference parentReference;

    /**
     * The Size.
     */
    @SerializedName("size")
    public Long size;

    /**
     * The Web Url.
     */
    @SerializedName("webUrl")
    public String webUrl;

    /**
     * The Audio.
     */
    @SerializedName("audio")
    public Audio audio;

    /**
     * The Deleted.
     */
    @SerializedName("deleted")
    public Deleted deleted;

    /**
     * The File.
     */
    @SerializedName("file")
    public File file;

    /**
     * The File System Info.
     */
    @SerializedName("fileSystemInfo")
    public FileSystemInfo fileSystemInfo;

    /**
     * The Folder.
     */
    @SerializedName("folder")
    public Folder folder;

    /**
     * The Image.
     */
    @SerializedName("image")
    public Image image;

    /**
     * The Location.
     */
    @SerializedName("location")
    public Location location;

    /**
     * The Open With.
     */
    @SerializedName("openWith")
    public OpenWithSet openWith;

    /**
     * The Photo.
     */
    @SerializedName("photo")
    public Photo photo;

    /**
     * The Search Result.
     */
    @SerializedName("searchResult")
    public SearchResult searchResult;

    /**
     * The Special Folder.
     */
    @SerializedName("specialFolder")
    public SpecialFolder specialFolder;

    /**
     * The Video.
     */
    @SerializedName("video")
    public Video video;
}
