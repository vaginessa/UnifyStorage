package org.cryse.unifystorage;

import java.io.InputStream;

public interface FileUpdater {
    void update(RemoteFile remoteFile, InputStream inputStream);
}
