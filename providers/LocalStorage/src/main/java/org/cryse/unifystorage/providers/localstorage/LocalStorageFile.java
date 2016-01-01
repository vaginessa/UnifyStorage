package org.cryse.unifystorage.providers.localstorage;

import org.cryse.unifystorage.FileDetail;
import org.cryse.unifystorage.FilePermission;
import org.cryse.unifystorage.HashAlgorithm;
import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.utils.hash.Sha1HashAlgorithm;

import java.io.File;
import java.util.Date;

public class LocalStorageFile implements RemoteFile {
    private File mFile;
    public LocalStorageFile(File file) {
        this.mFile = file;
    }

    public File getFile() {
        return mFile;
    }

    @Override
    public String getId() {
        return mFile.getAbsolutePath();
    }

    @Override
    public String getPath() {
        return mFile.getAbsolutePath();
    }

    @Override
    public boolean isDirectory() {
        return mFile.isDirectory();
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public String getHash() {
        return getHashAlgorithm().calculate(mFile);
    }

    @Override
    public long size() {
        return mFile.length();
    }

    @Override
    public String getFileType() {
        return null;
    }

    @Override
    public long lastModified() {
        return mFile.lastModified();
    }

    @Override
    public Date getLastModifiedTimeDate() {
        return new Date(mFile.lastModified());
    }

    @Override
    public long getCreateTime() {
        return 0;
    }

    @Override
    public Date getCreateTimeDate() {
        return new Date(0);
    }

    @Override
    public String getUrl() {
        return mFile.getAbsolutePath();
    }

    @Override
    public String getParentDirectoryPath() {
        return mFile.getParent();
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
