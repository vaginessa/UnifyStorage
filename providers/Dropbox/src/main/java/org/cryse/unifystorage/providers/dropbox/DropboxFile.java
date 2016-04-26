package org.cryse.unifystorage.providers.dropbox;

import org.cryse.unifystorage.FileDetail;
import org.cryse.unifystorage.FilePermission;
import org.cryse.unifystorage.HashAlgorithm;
import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.providers.dropbox.model.DropboxRawFile;
import org.cryse.unifystorage.utils.Path;

import java.util.Date;

public class DropboxFile extends DropboxRawFile implements RemoteFile {

    public DropboxFile() {
        id = "/";
        name = "/";
        pathLower = pathDisplay = "/";
        /*lastModified = 0;
        size = 0;
        isDirectory = true;*/
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
        return pathLower;
    }

    @Override
    public boolean isDirectory() {
        return type.equalsIgnoreCase("folder");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHash() {
        return null;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public String getFileType() {
        return null;
    }

    @Override
    public long lastModified() {
        return serverModified == null ? 0 : serverModified.getTime();
    }

    @Override
    public Date getLastModifiedTimeDate() {
        return serverModified;
    }

    @Override
    public long getCreateTime() {
        return 0;
    }

    @Override
    public Date getCreateTimeDate() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public String getParentDirectoryPath() {
        return Path.getDirectory(pathLower);
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
        return null;
    }
}
