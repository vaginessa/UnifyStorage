package org.cryse.unifystorage.providers.onedrive;

import android.text.TextUtils;

import org.cryse.unifystorage.FileDetail;
import org.cryse.unifystorage.FilePermission;
import org.cryse.unifystorage.HashAlgorithm;
import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.providers.onedrive.model.Item;
import org.cryse.unifystorage.utils.hash.Sha1HashAlgorithm;

import java.util.Date;

public class OneDriveFile extends Item implements RemoteFile {
    public static final String ROOT_PATH = "/drive/root:";

    /**
     * Creates an instance of RemoteFile
     *
     */
    protected OneDriveFile() {
    }


    @Override
    public boolean needsDownload() {
        return true;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPath() {
        String path = null;
        if (parentReference != null) {
            path = parentReference.path;
            if(!path.endsWith("/"))
                path = path + "/" + name;
            if (path.startsWith("/drive/root:"))
                path = path.substring("/drive/root:".length());
            if(!path.startsWith("/"))
                path = "/" + path;
        } else if(name.compareTo("root") == 0) {
            path = "/";
        }
        return path;
    }

    @Override
    public boolean isDirectory() {
        return folder != null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHash() {
        if(file != null && file.hashes != null) {
            return file.hashes.sha1Hash;
        }
        return null;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public String getFileType() {
        if(file != null && !TextUtils.isEmpty(file.mimeType)) {
            return file.mimeType;
        }
        return null;
    }

    @Override
    public long lastModified() {
        if(lastModifiedDateTime != null)
            return lastModifiedDateTime.getTimeInMillis();
        else
            return 0;
    }

    @Override
    public Date getLastModifiedTimeDate() {
        if(lastModifiedDateTime != null)
            return lastModifiedDateTime.getTime();
        else
            return null;
    }

    @Override
    public long getCreateTime() {
        if(createdDateTime != null)
            return createdDateTime.getTimeInMillis();
        else
            return 0;
    }

    @Override
    public Date getCreateTimeDate() {
        if(createdDateTime != null)
            return createdDateTime.getTime();
        else
            return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

    public String getParentDirectoryPath() {
        if(parentReference != null && !TextUtils.isEmpty(parentReference.path))
            return parentReference.path;
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
