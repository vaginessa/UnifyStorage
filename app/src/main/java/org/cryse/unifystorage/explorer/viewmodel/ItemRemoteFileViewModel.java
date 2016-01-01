package org.cryse.unifystorage.explorer.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.format.DateUtils;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.utils.FileSizeUtils;

public class ItemRemoteFileViewModel<RF extends RemoteFile> extends BaseObservable implements ViewModel {

    protected Context mContext;
    protected RF mRemoteFile;

    public ItemRemoteFileViewModel(Context context, RF remoteFile) {
        this.mContext = context;
        this.mRemoteFile = remoteFile;
    }

    public Context getContext() {
        return mContext;
    }

    public RF getRemoteFile() {
        return mRemoteFile;
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

    @Override
    public void destroy() {
        //In this case destroy doesn't need to do anything because there is not async calls
    }
}
