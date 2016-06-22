package org.cryse.widget;

import android.content.Context;

import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class SelectableRecyclerViewAdapter<ItemType extends Selectable> extends RecyclerArrayAdapter<ItemType> {
    private OnSelectionListener mOnSelectionListener;
    private List<ItemType> mSelectedItem = new ArrayList<>();

    public SelectableRecyclerViewAdapter(Context context) {
        super(context);
    }

    public SelectableRecyclerViewAdapter(Context context, ItemType[] objects) {
        super(context, objects);
    }

    public SelectableRecyclerViewAdapter(Context context, List<ItemType> objects) {
        super(context, objects);
    }

    public void replaceWith(List<ItemType> items) {
        clear();
        addAll(items);
    }

    @Override
    public void clear() {
        super.clear();
        clearSelection(false);
    }

    public void setSelection(int position, boolean selection) {
        int currentSelectionCount = mSelectedItem.size();
        if (selection) {
            mSelectedItem.add(getItem(position)); // select
        } else {
            mSelectedItem.remove(getItem(position)); // de-select
        }
        getItem(position).setSelected(selection);
        notifyItemChanged(position);
        int newSelectionCount = mSelectedItem.size();
        if (mOnSelectionListener != null) {
            if (currentSelectionCount == 0 && newSelectionCount > 0) {
                // Here onSelection() must called after onSelectionStart()
                mOnSelectionListener.onSelectionStart();
                mOnSelectionListener.onSelect(newSelectionCount, position);
            } else if (mSelectedItem.size() == 0) {
                // Here onSelection() must called before onSelectionStart()
                mOnSelectionListener.onDeselect(newSelectionCount, position);
                mOnSelectionListener.onSelectionEnd();
            } else {
                if(selection)
                    mOnSelectionListener.onSelect(newSelectionCount, position);
                else
                    mOnSelectionListener.onDeselect(newSelectionCount, position);
            }
        }
    }

    public void selectAll() {
        int itemCount = getCount();
        for(int position = 0; position < itemCount; position++) {
            getItem(position).setSelected(true);
        }
        notifyItemRangeChanged(0, itemCount);
        if (mOnSelectionListener !=null) {
            mOnSelectionListener.onSelect(itemCount);
        }
    }

    public void toggleSelection(int position) {
        setSelection(position, !isSelected(position));
    }

    public boolean isSelected(int position) {
        return mSelectedItem.contains(getItem(position));
    }

    public int getSelectionCount() {
        return mSelectedItem.size();
    }

    public void clearSelection() {
        clearSelection(true);
    }

    protected void clearSelection(boolean invalidateItems) {
        int lastSize = mSelectedItem.size();
        for (Iterator<ItemType> iterator = mSelectedItem.iterator(); iterator.hasNext(); ) {
            ItemType item = iterator.next();
            item.setSelected(false);
            iterator.remove();
            if(invalidateItems)
                notifyItemChanged(getPosition(item));
        }
        if (mOnSelectionListener != null && lastSize > 0) {
            mOnSelectionListener.onDeselect(0);
            mOnSelectionListener.onSelectionEnd();
        }
    }

    public int[] getSelections() {
        int selectionCount = mSelectedItem.size();
        int[] selections = new int[selectionCount];
        for (int i = 0; i < mSelectedItem.size(); i++) {
            ItemType item = mSelectedItem.get(i);
            selections[i] = getPosition(item);
        }
        return selections;
    }

    public ItemType[] getSelectionItems(Class<ItemType> itemTypeClass) {
        int selectionCount = mSelectedItem.size();
        final ItemType[] selectionItems = (ItemType[]) Array.newInstance(itemTypeClass, selectionCount);
        return mSelectedItem.toArray(selectionItems);
    }

    public boolean isInSelection() {
        return mSelectedItem.size() > 0;
    }

    public void setOnSelectionListener(OnSelectionListener listener) {
        this.mOnSelectionListener = listener;
    }

    public interface OnSelectionListener {
        void onSelectionStart();
        void onSelectionEnd();
        // When positions is null, means select all or clear selection
        void onSelect(int currentSelectionCount, int...positions);
        void onDeselect(int currentSelectionCount, int...positions);
    }
}
