package org.cryse.unifystorage;

import java.io.File;

public class LocalFile {
    private File mFile;

    public LocalFile(File file) {
        this.mFile = file;
    }

    public File getFile() {
        return mFile;
    }

    public String getFullPath() {
        return mFile.getPath();
    }

    public boolean isDirectory() {
        return mFile.isDirectory();
    }

    public String getName() {
        return mFile.getName();
    }

    public String getHashCode(HashAlgorithm hashAlgorithm) {
        return hashAlgorithm.calculate(mFile);
    }

    public long size() {
        return mFile.length();
    }

    public long getLastModifiedTime() {
        return mFile.lastModified();
    }

    public String getType() {
        return "";
    }
}
