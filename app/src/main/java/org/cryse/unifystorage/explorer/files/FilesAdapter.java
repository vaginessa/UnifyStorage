package org.cryse.unifystorage.explorer.files;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import org.cryse.widget.SelectableRecyclerViewAdapter;

import java.util.ArrayList;

public class FilesAdapter extends SelectableRecyclerViewAdapter<RemoteFileWrapper> {
    /*private OnFileClickListener mOnFileClickListener;*/

    public FilesAdapter(Context context) {
        super(context, new ArrayList<RemoteFileWrapper>());
        setHasStableIds(false);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new FileViewHolder(parent);
    }

    @Override
    public void OnBindViewHolder(BaseViewHolder holder, int position) {
        super.OnBindViewHolder(holder, position);
    }

    /*public void setOnFileClickListener(OnFileClickListener onFileClickListener) {
        this.mOnFileClickListener = onFileClickListener;
    }

    public interface OnFileClickListener {
        void onFileClick(View view, int position, RemoteFile file);
        void onFileLongClick(View view, int position, RemoteFile file);
    }*/
}