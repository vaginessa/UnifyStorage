package org.cryse.unifystorage;

public interface AbstractFile {
    String getName();
    long getLastModifiedTime();
    long size();
}
