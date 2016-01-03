package org.cryse.unifystorage.providers.dropbox;

import com.dropbox.core.v2.DbxFiles;

import org.cryse.unifystorage.FileDetail;
import org.cryse.unifystorage.FilePermission;
import org.cryse.unifystorage.HashAlgorithm;
import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.utils.Path;

import java.util.Date;

public class DropboxFile implements RemoteFile {
    private DbxFiles.Metadata metadata;
    private String id;
    private String name;
    private String path;
    private long lastModified;
    private long size;
    private boolean isDirectory;

    public DropboxFile() {
        id = "/";
        name = "/";
        path = "/";
        lastModified = 0;
        size = 0;
        isDirectory = true;
    }

    public DropboxFile(DbxFiles.Metadata metadata) {
        init(metadata);
    }

    private void init(
            DbxFiles.Metadata metadata
    ) {
        this.metadata = metadata;
        if(metadata instanceof DbxFiles.FileMetadata) {
            DbxFiles.FileMetadata fileMetadata = (DbxFiles.FileMetadata)metadata;
            id = fileMetadata.id;
            name = fileMetadata.name;
            path = fileMetadata.pathLower;
            lastModified = fileMetadata.serverModified.getTime();
            size = fileMetadata.size;
            isDirectory = false;
        } else if (metadata instanceof DbxFiles.FolderMetadata) {
            DbxFiles.FolderMetadata folderMetadata = (DbxFiles.FolderMetadata)metadata;
            id = folderMetadata.id;
            name = folderMetadata.name;
            path = folderMetadata.pathLower;
            lastModified = 0;
            size = 0;
            isDirectory = true;
        } else if (metadata instanceof DbxFiles.DeletedMetadata) {
            DbxFiles.DeletedMetadata deletedMetadata = (DbxFiles.DeletedMetadata)metadata;
            id = deletedMetadata.name;
            name = deletedMetadata.name;
            path = deletedMetadata.pathLower;
            lastModified = 0;
            size = 0;
            isDirectory = false;
        } else {
            throw new IllegalArgumentException("Unknown Dropbox metadata");
        }
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
        return lastModified;
    }

    @Override
    public Date getLastModifiedTimeDate() {
        return new Date(lastModified);
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
        return Path.getDirectory(path);
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
