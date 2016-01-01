package org.cryse.unifystorage.providers.onedrive;

import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.onedrive.sdk.extensions.Item;

import org.cryse.unifystorage.FileDetail;
import org.cryse.unifystorage.FilePermission;
import org.cryse.unifystorage.HashAlgorithm;
import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.utils.hash.Sha1HashAlgorithm;

import java.util.Date;

public class OneDriveFile implements RemoteFile {
    public static final String ROOT_PATH = "/drive/root:";
    private String id;
    private String name;
    private String path;
    private long size;
    private String downloadUrl;
    private String type;
    private String hash;
    private boolean isDirectory;
    private Date lastModified;
    private Date createTime;
    private Item model;

    /**
     * Creates an instance of RemoteFile
     *
     * @param oneDriveItem OneDrive item metadata.
     */
    protected OneDriveFile(Item oneDriveItem) {
        initializeValue(oneDriveItem);
    }

    private boolean initializeValue(Item oneDriveItem) {
        this.model = oneDriveItem;
        this.id = oneDriveItem.id;
        this.name = oneDriveItem.name;
        if(this.name.equalsIgnoreCase("root") && !oneDriveItem.getRawObject().has("parentReference"))
            this.path = "/";
        else if(oneDriveItem.getRawObject().has("parentReference") ) {
            JsonObject parentReference = oneDriveItem.getRawObject().getAsJsonObject("parentReference");
            if (parentReference.has("path")) {
                this.path = parentReference.get("path").getAsString();
                if(!this.path.endsWith("/"))
                    this.path = this.path + "/" + this.name;
                if (path.startsWith("/drive/root:"))
                    this.path = this.path.substring("/drive/root:".length());
                if(!this.path.startsWith("/"))
                    this.path = "/" + this.path;
            }
        }
        if(TextUtils.isEmpty(this.path)) this.path = "name";
        if(oneDriveItem.getRawObject().has("@content.downloadUrl"))
            this.downloadUrl = oneDriveItem.getRawObject().get("@content.downloadUrl").getAsString();
        if(oneDriveItem.file != null) {
            if(!TextUtils.isEmpty(oneDriveItem.file.mimeType))
                this.type = oneDriveItem.file.mimeType;
            this.hash = oneDriveItem.file.hashes.sha1Hash;
        }
        this.isDirectory = oneDriveItem.folder != null;
        this.size = oneDriveItem.size;
        this.lastModified = oneDriveItem.lastModifiedDateTime.getTime();
        this.createTime = oneDriveItem.createdDateTime.getTime();
        return true;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public String getFileType() {
        return type;
    }

    @Override
    public long lastModified() {
        return lastModified.getTime();
    }

    @Override
    public Date getLastModifiedTimeDate() {
        return lastModified;
    }

    @Override
    public long getCreateTime() {
        return createTime.getTime();
    }

    @Override
    public Date getCreateTimeDate() {
        return createTime;
    }

    @Override
    public String getUrl() {
        return downloadUrl;
    }

    public String getParentDirectoryPath() {
        if(model.parentReference != null && !TextUtils.isEmpty(model.parentReference.path))
            return model.parentReference.path;
        else
            return ROOT_PATH;
    }

    @Override
    public FileDetail getDetail() {
        return null;
    }

    @Override
    public FilePermission getPermission() {
        return null;
    }

    @Override
    public HashAlgorithm getHashAlgorithm() {
        return Sha1HashAlgorithm.getInstance();
    }
}
