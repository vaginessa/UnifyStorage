package org.cryse.unifystorage.explorer.files;


import android.util.Log;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.cryse.unifystorage.utils.Path;
import org.cryse.utils.file.OnFileChangedListener;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;

public class FileAlterationWatcher implements LocalFileWatcher {
    public static final String LOG_TAG = FileAlterationWatcher.class.getSimpleName();
    public static final long DEFAULT_INTERVAL = 10000;
    private final FileAlterationMonitor mFileMonitor;
    private OnFileChangedListener mOnFileChangedListener;

    public FileAlterationWatcher() {
        this(DEFAULT_INTERVAL);
    }

    public FileAlterationWatcher(long interval) {
        mFileMonitor = new FileAlterationMonitor(interval);
        try {
            mFileMonitor.start();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    @Override
    public void startWatching(String path) {
        String cleanedPath = Path.getLocalCanonicalPath(path);
        boolean exists = false;
        for(Iterator<FileAlterationObserver> iterator = mFileMonitor.getObservers().iterator(); iterator.hasNext();) {
            FileAlterationObserver observer = iterator.next();
            String observingPath = Path.getLocalCanonicalPath(observer.getDirectory());
            if(cleanedPath.compareTo(observingPath) == 0) {
                // Already watching
                exists = true;
                break;
            }
        }
        if(!exists) {
            FileAlterationObserver observer = new FileAlterationObserver(cleanedPath, new SingleLevelFileFilter(new File(cleanedPath)));
            try {
                observer.initialize();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }

            observer.addListener(mAlterationListener);
            mFileMonitor.addObserver(observer);
        }
    }

    @Override
    public void stopWatching(String path) {
        String cleanedPath = Path.getLocalCanonicalPath(path);
        for(Iterator<FileAlterationObserver> iterator = mFileMonitor.getObservers().iterator(); iterator.hasNext();) {
            FileAlterationObserver observer = iterator.next();
            String observingPath = Path.getLocalCanonicalPath(observer.getDirectory());
            if(cleanedPath.compareTo(observingPath) == 0) {
                // Already watching
                observer.removeListener(mAlterationListener);
                mFileMonitor.removeObserver(observer);
                break;
            }
        }
    }

    @Override
    public void stopWatchingAll() {
        for(Iterator<FileAlterationObserver> iterator = mFileMonitor.getObservers().iterator(); iterator.hasNext();) {
            FileAlterationObserver observer = iterator.next();
            observer.removeListener(mAlterationListener);
            mFileMonitor.removeObserver(observer);
        }
    }

    @Override
    public void destroy() {
        stopWatchingAll();
        try {
            mFileMonitor.stop(5000);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    @Override
    public void setOnFileChangeListener(OnFileChangedListener onFileChangedListener) {
        this.mOnFileChangedListener = onFileChangedListener;
    }

    private FileAlterationListener mAlterationListener = new FileAlterationListener() {
        @Override
        public void onStart(FileAlterationObserver fileAlterationObserver) {

        }

        @Override
        public void onDirectoryCreate(File file) {
            if(mOnFileChangedListener != null) {
                mOnFileChangedListener.onFileCreate(file.getPath(), file.getPath());
            }
        }

        @Override
        public void onDirectoryChange(File file) {
            if(mOnFileChangedListener != null) {
                mOnFileChangedListener.onFileModify(file.getPath(), file.getPath());
            }
        }

        @Override
        public void onDirectoryDelete(File file) {
            if(mOnFileChangedListener != null) {
                mOnFileChangedListener.onFileDelete(file.getPath(), file.getPath());
            }
        }

        @Override
        public void onFileCreate(File file) {
            if(mOnFileChangedListener != null) {
                mOnFileChangedListener.onFileCreate(file.getPath(), file.getPath());
            }

        }

        @Override
        public void onFileChange(File file) {
            if(mOnFileChangedListener != null) {
                mOnFileChangedListener.onFileModify(file.getPath(), file.getPath());
            }
        }

        @Override
        public void onFileDelete(File file) {
            if(mOnFileChangedListener != null) {
                mOnFileChangedListener.onFileDelete(file.getPath(), file.getPath());
            }
        }

        @Override
        public void onStop(FileAlterationObserver fileAlterationObserver) {

        }
    };

    public static class SingleLevelFileFilter implements FileFilter {

        private File parentDirectory;

        public SingleLevelFileFilter(File parentDirectory) {
            this.parentDirectory = parentDirectory;

        }

        @Override
        public boolean accept(File pathName) {
            if (!pathName.getParentFile().equals(parentDirectory)) {
                return false;
            }

            return true;
        }

    }
}
