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
import org.cryse.utils.selector.SelectableRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends SelectableRecyclerViewAdapter<
        RemoteFile,
        List<RemoteFile>,
        FileAdapter.BindingHolder
        > {
    private Context mContext;
    private OnFileClickListener mOnFileClickListener;

    public FileAdapter(Context context) {
        this.mContext = context;
        setHasStableIds(false);
    }

    @Override
    public List<RemoteFile> buildItemsCollection() {
        return new ArrayList<>();
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
        fileBinding.executePendingBindings();
        // ATE.apply(fileBinding.itemFileRootContainer,mATEKey);
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

    public interface OnFileClickListener {
        void onFileClick(View view, int position, RemoteFile file);
        void onFileLongClick(View view, int position, RemoteFile file);
    }
}
