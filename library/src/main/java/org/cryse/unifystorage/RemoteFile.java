package org.cryse.unifystorage;

public interface RemoteFile {
    String getId();

    String getFullPath();

    boolean isDirectory();

    String getName();

    String getHashCode();

    long size();

    long getLastModifiedTime();

    String getUrl();

    FileDetail getDetail();

    FilePermission getPermission();
}
