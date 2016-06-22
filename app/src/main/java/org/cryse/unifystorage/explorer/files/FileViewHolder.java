package org.cryse.unifystorage.explorer.files;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.utils.FileSizeUtils;

public class FileViewHolder extends BaseViewHolder<RemoteFileWrapper> {
    public ImageView mIconView;
    public TextView mNameView;
    public TextView mDetail1View;
    public TextView mDetail2View;

    public FileViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_file);
        mIconView = $(R.id.item_file_icon);
        mNameView = $(R.id.item_file_name);
        mDetail1View = $(R.id.item_file_detail1);
        mDetail2View = $(R.id.item_file_detail2);
    }

    @Override
    public void setData(final RemoteFileWrapper file){
        itemView.setSelected(file.isSelected());
        mIconView.setImageResource(file.getRemoteFile().isDirectory() ? R.drawable.ic_format_folder : R.drawable.ic_format_file);
        mNameView.setText(file.getRemoteFile().getName());
        mDetail1View.setText(getDetail(file.getRemoteFile()));
        mDetail2View.setText(getDetail2(file.getRemoteFile()));
        /*itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnFileClickListener != null)
                    mOnFileClickListener.onFileClick(v, position, file);
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mOnFileClickListener != null) {
                    mOnFileClickListener.onFileLongClick(v, position, file);
                    return true;
                }
                return false;
            }
        });*/
    }



    public String getDetail(RemoteFile file) {
        Context context = itemView.getContext();
        if(file.isDirectory())
            return context.getString(R.string.tag_directory);
        else {
            return FileSizeUtils.humanReadableByteCount(file.size(), false);
        }
    }

    public String getDetail2(RemoteFile file) {
        Context context = itemView.getContext();
        if(file.getLastModifiedTimeDate() != null)
            return DateUtils.formatDateTime(context, file.lastModified(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        else
            return "";
    }
}
