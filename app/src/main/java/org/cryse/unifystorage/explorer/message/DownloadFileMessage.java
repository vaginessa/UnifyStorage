package org.cryse.unifystorage.explorer.message;

import android.content.DialogInterface;

public class DownloadFileMessage extends BasicMessage {
    protected static final int MSG_TYPE_DOWNLOAD = 1;
    public static final int MSG_TYPE = MSG_TYPE_DOWNLOAD;
    protected long mFileSize;
    protected long mCurrentSize;
    protected DialogInterface.OnDismissListener mOnDismissListener;

    public DownloadFileMessage(long id, String title, String content) {
        super(id, title, content);
    }

    public DownloadFileMessage(long id, String title, String content, boolean hide) {
        super(id, title, content, hide);
    }

    public long getFileSize() {
        return mFileSize;
    }

    public void setFileSize(long fileSize) {
        this.mFileSize = fileSize;
    }

    public long getCurrentSize() {
        return mCurrentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.mCurrentSize = currentSize;
    }

    public DialogInterface.OnDismissListener getOnDismissListener() {
        return mOnDismissListener;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.mOnDismissListener = onDismissListener;
    }

    public int getMsgType() {
        return MSG_TYPE_DOWNLOAD;
    }
}
