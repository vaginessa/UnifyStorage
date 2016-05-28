package org.cryse.unifystorage.explorer.utils.cache;

import android.net.Uri;

import org.cryse.unifystorage.RemoteFile;

import java.io.File;

public interface FileCacheRepository {
    String getFullCachePathForFile(
            String providerName,
            String providerUuid,
            RemoteFile remoteFile);
    String getUriForFile(File file);
}
