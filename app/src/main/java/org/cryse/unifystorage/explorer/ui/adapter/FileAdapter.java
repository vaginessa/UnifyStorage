package org.cryse.unifystorage.explorer.ui.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.databinding.ItemFileBinding;
import org.cryse.unifystorage.explorer.viewmodel.ItemRemoteFileViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileAdapter<RF extends RemoteFile> extends RecyclerView.Adapter<FileAdapter.BindingHolder> {
    private List<RF> mFiles;
    private Context mContext;
    private OnFileClickListener mOnFileClickListener;

    public FileAdapter(Context context) {
        this.mContext = context;
        this.mFiles = new ArrayList<>();
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemFileBinding fileBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_file,
                parent,
                false);
        return new BindingHolder(fileBinding);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        ItemFileBinding fileBinding = holder.binding;
        fileBinding.setViewModel(new ItemRemoteFileViewModel<>(mContext, mFiles.get(position)));
        fileBinding.setClickListener(this);
        fileBinding.setAdapterPosition(position);
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public void replaceWith(Collection<RF> items) {
        replaceWith(items, false);
    }

    public void replaceWith(Collection<RF> items, boolean cleanToReplace) {
        if(items == null) return;
        if(cleanToReplace) {
            clear();
            addAll(items);
        } else {
            int oldCount = mFiles.size();
            int newCount = items.size();
            int delCount = oldCount - newCount;
            mFiles.clear();
            mFiles.addAll(items);
            if(delCount > 0) {
                notifyItemRangeChanged(0, newCount);
                notifyItemRangeRemoved(newCount, delCount);
            } else if(delCount < 0) {
                notifyItemRangeChanged(0, oldCount);
                notifyItemRangeInserted(oldCount, - delCount);
            } else {
                notifyItemRangeChanged(0, newCount);
            }
        }
    }

    public void addItem(RF remoteFile) {
        if(remoteFile == null) return;
        if (!mFiles.contains(remoteFile)) {
            mFiles.add(remoteFile);
            notifyItemInserted(mFiles.size() - 1);
        } else {
            mFiles.set(mFiles.indexOf(remoteFile), remoteFile);
            notifyItemChanged(mFiles.indexOf(remoteFile));
        }
    }

    public void addAll(Collection<RF> files) {
        if(files == null) return;
        int currentCount = mFiles.size();
        int newFilesCount = files.size();
        mFiles.addAll(files);
        notifyItemRangeInserted(currentCount, newFilesCount);
    }

    public void clear() {
        int size = mFiles.size();
        mFiles.clear();
        notifyItemRangeRemoved(0, size);
    }

    public static class BindingHolder extends RecyclerView.ViewHolder {
        private ItemFileBinding binding;

        public BindingHolder(ItemFileBinding binding) {
            super(binding.itemFileRootContainer);
            this.binding = binding;
        }
    }

    public void setOnFileClickListener(OnFileClickListener onFileClickListener) {
        this.mOnFileClickListener = onFileClickListener;
    }

    public void onItemClick(View view) {
        if(mOnFileClickListener != null) {
            int adapterPosition = (int)view.getTag();
            mOnFileClickListener.onFileClick(view, adapterPosition, mFiles.get(adapterPosition));
        }
    }

    public boolean onItemLongClick(View view) {
        if(mOnFileClickListener != null) {
            int adapterPosition = (int)view.getTag();
            mOnFileClickListener.onFileLongClick(view, adapterPosition, mFiles.get(adapterPosition));
        }
        return true;
    }

    public interface OnFileClickListener<RF> {
        void onFileClick(View view, int position, RF file);
        void onFileLongClick(View view, int position, RF file);
    }
}
