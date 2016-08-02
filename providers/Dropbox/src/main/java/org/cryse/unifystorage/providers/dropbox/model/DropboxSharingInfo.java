package org.cryse.unifystorage.providers.dropbox.model;

import com.google.gson.annotations.SerializedName;

public class DropboxSharingInfo {
    @SerializedName("read_only")
    public boolean readOnly;
    @SerializedName("parent_shared_folder_id")
    public String parentSharedFolderId;
    @SerializedName("modified_by")
    public String modifiedBy;
}
