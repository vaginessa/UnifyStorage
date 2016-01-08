package org.cryse.unifystorage.explorer.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.format.DateUtils;
import android.view.View;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.ui.adapter.FileAdapter;
import org.cryse.unifystorage.utils.FileSizeUtils;

public class ItemRemoteFileViewModel<RF extends RemoteFile> extends BaseObservable implements ViewModel {

    protected Context mContext;
    protected RF mRemoteFile;
    protected int mAdapterPosition;
    private FileAdapter.OnFileClickListener<RF> mOnFileClickListener;

    public ItemRemoteFileViewModel(Context context, int adapterPosition, RF remoteFile) {
        this.mContext = context;
        this.mAdapterPosition = adapterPosition;
        this.mRemoteFile = remoteFile;
    }

    public Context getContext() {
        return mContext;
    }

    public void setRemoteFile(RF remoteFile) {
        this.mRemoteFile = remoteFile;
    }

    public RF getRemoteFile() {
        return mRemoteFile;
    }

    public int getAdapterPosition() {
        return mAdapterPosition;
    }

    public void setAdapterPosition(int adapterPosition) {
        this.mAdapterPosition = adapterPosition;
    }

    public String getFileName() {
        return mRemoteFile.getName();
    }

    public long getFileSize() {
        return mRemoteFile.size();
    }

    public String getLastModified() {
        return mRemoteFile.getLastModifiedTimeDate().toString();
    }

    public String getDetail() {
        if(mRemoteFile.isDirectory())
            return mContext.getString(R.string.tag_directory);
        else {
            return FileSizeUtils.humanReadableByteCount(mRemoteFile.size(), false);
        }
    }

    public String getDetail2() {
        return DateUtils.formatDateTime(mContext, mRemoteFile.lastModified(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
    }

    public Drawable getIcon() {
        if(mRemoteFile.isDirectory())
            return ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_file_type_folder, null);
        else {
            return ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_file_type_file, null);
        }
    }

    public void onItemClick(View view) {
        if(mOnFileClickListener != null) {
            mOnFileClickListener.onFileClick(view, mAdapterPosition, mRemoteFile);
        }
    }

    public boolean onItemLongClick(View view) {
        if(mOnFileClickListener != null) {
            mOnFileClickListener.onFileLongClick(view, mAdapterPosition, mRemoteFile);
        }
        return true;
    }

    public void setOnFileClickListener(FileAdapter.OnFileClickListener<RF> onFileClickListener) {
        this.mOnFileClickListener = onFileClickListener;
    }

    @Override
    public void destroy() {
        //In this case destroy doesn't need to do anything because there is not async calls
    }
}
