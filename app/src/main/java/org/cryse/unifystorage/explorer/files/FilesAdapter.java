package org.cryse.unifystorage.explorer.files;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.utils.FileSizeUtils;
import org.cryse.utils.selector.SelectableRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FilesAdapter extends SelectableRecyclerViewAdapter<
        RemoteFile,
        List<RemoteFile>,
        FilesAdapter.ViewHolder
        > {
    private Context mContext;
    private OnFileClickListener mOnFileClickListener;

    public FilesAdapter(Context context) {
        this.mContext = context;
        setHasStableIds(false);
    }

    @Override
    public List<RemoteFile> buildItemsCollection() {
        return new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final RemoteFile file = getItems().get(position);
        holder.itemView.setSelected(isSelected(position));
        holder.mIconView.setImageResource(file.isDirectory() ? R.drawable.ic_format_folder : R.drawable.ic_format_file);
        holder.mNameView.setText(file.getName());
        holder.mDetail1View.setText(getDetail(file));
        holder.mDetail2View.setText(getDetail2(file));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnFileClickListener != null)
                    mOnFileClickListener.onFileClick(v, position, file);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mOnFileClickListener != null) {
                    mOnFileClickListener.onFileLongClick(v, position, file);
                    return true;
                }
                return false;
            }
        });

        /*ItemFileBinding fileBinding = holder.binding;
        ItemRemoteFileViewModel viewModel = fileBinding.getViewModel();
        if (viewModel == null) {
            viewModel = new ItemRemoteFileViewModel(mContext, position, getItems().get(position));
            viewModel.setOnFileClickListener(this.mOnFileClickListener);
            fileBinding.setViewModel(viewModel);
        } else {
            viewModel.setAdapterPosition(position);
            viewModel.setRemoteFile(getItem(position));
        }
        fileBinding.setItemSelected(isSelected(position));
        fileBinding.getViewModel().notifyChange();
        fileBinding.executePendingBindings();*/
        // ATE.apply(fileBinding.itemFileRootContainer,mATEKey);
    }

    public String getDetail(RemoteFile file) {
        if(file.isDirectory())
            return mContext.getString(R.string.tag_directory);
        else {
            return FileSizeUtils.humanReadableByteCount(file.size(), false);
        }
    }

    public String getDetail2(RemoteFile file) {
        if(file.getLastModifiedTimeDate() != null)
            return DateUtils.formatDateTime(mContext, file.lastModified(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        else
            return "";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.item_file_icon)
        public ImageView mIconView;
        @Bind(R.id.item_file_name)
        public TextView mNameView;
        @Bind(R.id.item_file_detail1)
        public TextView mDetail1View;
        @Bind(R.id.item_file_detail2)
        public TextView mDetail2View;
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public void setOnFileClickListener(OnFileClickListener onFileClickListener) {
        this.mOnFileClickListener = onFileClickListener;
    }

    public interface OnFileClickListener {
        void onFileClick(View view, int position, RemoteFile file);
        void onFileLongClick(View view, int position, RemoteFile file);
    }
}