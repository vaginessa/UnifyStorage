package org.cryse.unifystorage;

public interface AbstractFile {
    String getPath();
    String getFileType();
    boolean isDirectory();
    String getName();
    long lastModified();
    long size();
}
