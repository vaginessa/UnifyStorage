package org.cryse.unifystorage.explorer.files;

import org.cryse.unifystorage.utils.Path;
import org.cryse.utils.file.OnFileChangedListener;
import org.cryse.utils.file.SimpleFileObserver;

import java.util.HashMap;

public class FileObserverWatcher implements LocalFileWatcher {
    public static final String LOG_TAG = FileObserverWatcher.class.getName();

    // private FileObserver mFileObserver;
    private OnFileChangedListener mOnFileChangedListener;
    private HashMap<String, SimpleFileObserver> mObserversMap;

    public FileObserverWatcher() {
        mObserversMap = new HashMap<>();
    }

    @Override
    public void startWatching(String path) {
        String cleanedPath = Path.getLocalCanonicalPath(path);
        if(mObserversMap.containsKey(cleanedPath)) {
            // Exists
            SimpleFileObserver observer = mObserversMap.get(cleanedPath);
            observer.startWatching();
        } else {
            SimpleFileObserver observer = new SimpleFileObserver(cleanedPath);
            observer.setOnFileChangedListener(mInternalListener);
            observer.startWatching();
            mObserversMap.put(cleanedPath, observer);
        }
    }

    @Override
    public void stopWatching(String path) {
        String cleanedPath = Path.getLocalCanonicalPath(path);
        if(mObserversMap.containsKey(cleanedPath)) {
            // Exists
            SimpleFileObserver observer = mObserversMap.get(cleanedPath);
            observer.stopWatching();
            observer.removeOnFileChangedListener();
            mObserversMap.remove(cleanedPath);
        }
    }

    @Override
    public void stopWatchingAll() {
        for(SimpleFileObserver observer : mObserversMap.values()) {
            observer.stopWatching();
            observer.removeOnFileChangedListener();
        }
        mObserversMap.clear();
    }

    private OnFileChangedListener mInternalListener = new OnFileChangedListener() {
        @Override
        public boolean onFileCreate(String path, String file) {
            return mOnFileChangedListener != null && mOnFileChangedListener.onFileCreate(path, file);
        }

        @Override
        public boolean onFileDelete(String path, String file) {
            return mOnFileChangedListener != null && mOnFileChangedListener.onFileDelete(path, file);
        }

        @Override
        public boolean onFileModify(String path, String file) {
            return mOnFileChangedListener != null && mOnFileChangedListener.onFileModify(path, file);
        }

        @Override
        public boolean onFileEvent(int event, String path, String file) {
            return mOnFileChangedListener != null && mOnFileChangedListener.onFileEvent(event, path, file);
        }
    };

    @Override
    public void destroy() {
        stopWatchingAll();
    }

    @Override
    public void setOnFileChangeListener(OnFileChangedListener onFileChangedListener) {
        this.mOnFileChangedListener = onFileChangedListener;
    }
}
