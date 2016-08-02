package org.cryse.unifystorage.explorer.files;

import org.cryse.utils.file.OnFileChangedListener;

public interface LocalFileWatcher {
    void startWatching(String path);

    void stopWatching(String path);

    void stopWatchingAll();

    void destroy();

    void setOnFileChangeListener(OnFileChangedListener onFileChangedListener);
}
