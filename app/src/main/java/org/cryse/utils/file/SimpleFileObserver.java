package org.cryse.utils.file;

import android.os.FileObserver;
import android.util.Log;

import java.io.File;

public class SimpleFileObserver extends FileObserver {
    private static final String LOG_TAG = SimpleFileObserver.class.getSimpleName();
    private String mRootPath;
    private OnFileChangedListener mOnFileChangedListener;

    public SimpleFileObserver(String path) {
        super(path, FileObserver.ALL_EVENTS);
        init(path);
    }

    public SimpleFileObserver(String path, int mask) {
        super(path, mask);
        init(path);
    }

    private void init(String path) {
        if (!path.endsWith(File.separator)){
            path += File.separator;
        }
        this.mRootPath = path;
    }

    @Override
    public void onEvent(int event, String path) {
        event &= FileObserver.ALL_EVENTS;
        String filePath = mRootPath + path;
        switch(event){
            case FileObserver.CREATE:
                Log.d(LOG_TAG, "CREATE:" + filePath);
                if(mOnFileChangedListener != null) {
                    boolean ret = mOnFileChangedListener.onFileCreate(mRootPath, path);
                    if(!ret) mOnFileChangedListener.onFileEvent(event, mRootPath, path);
                }
                break;
            case FileObserver.DELETE:
                Log.d(LOG_TAG, "DELETE:" + filePath);
                if(mOnFileChangedListener != null) {
                    boolean ret = mOnFileChangedListener.onFileDelete(mRootPath, path);
                    if(!ret) mOnFileChangedListener.onFileEvent(event, mRootPath, path);
                }
                break;
            case FileObserver.MODIFY:
                Log.d(LOG_TAG, "MODIFY:" + filePath);
                if(mOnFileChangedListener != null) {
                    boolean ret = mOnFileChangedListener.onFileModify(mRootPath, path);
                    if(!ret) mOnFileChangedListener.onFileEvent(event, mRootPath, path);
                }
                break;
            case FileObserver.DELETE_SELF:
                Log.d(LOG_TAG, "DELETE_SELF:" + filePath);
                if(mOnFileChangedListener != null) {
                    mOnFileChangedListener.onFileEvent(event, mRootPath, path);
                }
                break;
            case FileObserver.MOVED_FROM:
                Log.d(LOG_TAG, "MOVED_FROM:" + filePath);
                if(mOnFileChangedListener != null) {
                    mOnFileChangedListener.onFileEvent(event, mRootPath, path);
                }
                break;
            case FileObserver.MOVED_TO:
                Log.d(LOG_TAG, "MOVED_TO:" + path);
                if(mOnFileChangedListener != null) {
                    mOnFileChangedListener.onFileEvent(event, mRootPath, path);
                }
                break;
            case FileObserver.MOVE_SELF:
                Log.d(LOG_TAG, "MOVE_SELF:" + path);
                if(mOnFileChangedListener != null) {
                    mOnFileChangedListener.onFileEvent(event, mRootPath, path);
                }
                break;
            default:
                // just ignore
                break;
        }
    }

    public void setOnFileChangedListener(OnFileChangedListener onFileChangedListener) {
        this.mOnFileChangedListener = onFileChangedListener;
    }

    public void removeOnFileChangedListener() {
        this.mOnFileChangedListener = null;
    }
}
