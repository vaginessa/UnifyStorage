package org.cryse.unifystorage;

import java.util.Date;

public interface RemoteFile extends AbstractFile{
    String getId();

    String getAbsolutePath();

    boolean isDirectory();

    String getName();

    String getHash();

    long size();

    String getFileType();

    long getLastModifiedTime();

    Date getLastModifiedTimeDate();

    long getCreateTime();

    Date getCreateTimeDate();

    String getUrl();

    String getParentDirectoryPath();

    FileDetail getDetail();

    FilePermission getPermission();

    HashAlgorithm getHashAlgorithm();
}
