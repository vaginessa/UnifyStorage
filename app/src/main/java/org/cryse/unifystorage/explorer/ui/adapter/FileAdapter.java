package org.cryse.unifystorage.explorer.ui.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.materialcab.Util;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.databinding.ItemFileBinding;
import org.cryse.unifystorage.explorer.viewmodel.ItemRemoteFileViewModel;
import org.cryse.utils.selector.SelectableRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter<RF extends RemoteFile>
        extends SelectableRecyclerViewAdapter<
        RF,
        List<RF>,
        FileAdapter.BindingHolder
        > {
    private Context mContext;
    private OnFileClickListener<RF> mOnFileClickListener;

    public FileAdapter(Context context) {
        this.mContext = context;
        setHasStableIds(false);
    }

    @Override
    public List<RF> buildItemsCollection() {
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
        if (fileBinding.getViewModel() == null) {
            ItemRemoteFileViewModel viewModel = new ItemRemoteFileViewModel<>(mContext, position, getItems().get(position));
            viewModel.setOnFileClickListener(this.mOnFileClickListener);
            fileBinding.setViewModel(viewModel);
        } else {
            fileBinding.getViewModel().setAdapterPosition(position);
            fileBinding.getViewModel().setRemoteFile(getItem(position));
        }
        fileBinding.setItemSelected(isSelected(position));
        fileBinding.getViewModel().notifyChange();
        fileBinding.executePendingBindings();
        ATE.apply(
                fileBinding.itemFileRootContainer,
                Util.resolveString(fileBinding.itemFileRootContainer.getContext(), R.attr.ate_key)
        );
    }

    public static class BindingHolder extends RecyclerView.ViewHolder {
        private ItemFileBinding binding;

        public BindingHolder(ItemFileBinding binding) {
            super(binding.itemFileRootContainer);
            this.binding = binding;
        }
    }

    public void setOnFileClickListener(OnFileClickListener<RF> onFileClickListener) {
        this.mOnFileClickListener = onFileClickListener;
    }

    public interface OnFileClickListener<RF> {
        void onFileClick(View view, int position, RF file);
        void onFileLongClick(View view, int position, RF file);
    }
}
