package org.cryse.unifystorage;

import java.util.Date;

public interface RemoteFile extends AbstractFile {
    boolean needsDownload();

    String getId();

    @Override
    String getPath();

    @Override
    boolean isDirectory();

    @Override
    String getName();

    String getHash();

    @Override
    long size();

    @Override
    String getFileType();

    @Override
    long lastModified();

    Date getLastModifiedTimeDate();

    long getCreateTime();

    Date getCreateTimeDate();

    String getUrl();

    String getParentDirectoryPath();

    FileDetail getDetail();

    FilePermission getPermission();

    HashAlgorithm getHashAlgorithm();
}
