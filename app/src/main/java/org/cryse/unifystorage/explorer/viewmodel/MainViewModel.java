package org.cryse.unifystorage.explorer.viewmodel;

import android.content.Context;

import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.utils.DrawerItemUtils;
import org.cryse.unifystorage.providers.localstorage.utils.LocalStorageUtils;
import org.cryse.unifystorage.utils.Path;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel implements ViewModel {
    private IDrawerItem[] mDrawerItems;
    private Context mContext;
    private DataListener mDataListener;
    private int mCurrentSelectionIdentifier;

    public MainViewModel(DataListener mDataListener, Context mContext) {
        this.mDataListener = mDataListener;
        this.mContext = mContext;
    }

    @Override
    public void destroy() {

    }

    public void updateDrawerItems() {
        buildDrawerItems();
        mDataListener.onDrawerItemsChanged(mDrawerItems);
    }

    private void buildDrawerItems() {
        // Firstly get all local storage devices:
        List<IDrawerItem> drawerItems = new ArrayList<>();
        String[] externalStoragePaths = LocalStorageUtils.getStorageDirectories(mContext);
        int[] externalStorageTypes = DrawerItemUtils.getStorageDirectoryTypes(mContext, externalStoragePaths);
        int otherStorageProvidersCount = 0;
        int constDrawerItemsCount = 5;
        mDrawerItems = new IDrawerItem[externalStoragePaths.length + otherStorageProvidersCount + constDrawerItemsCount];

        for (int i = 0; i < externalStoragePaths.length; i++) {
            String path = externalStoragePaths[i];
            int type = externalStorageTypes[i];
            switch (type) {
                case DrawerItemUtils.STORAGE_DIRECTORY_INTERNAL_STORAGE:
                    drawerItems.add(new PrimaryDrawerItem().withName(mContext.getString(R.string.drawer_local_internal_storage))
                            .withTag(path)
                            .withIcon(R.drawable.ic_drawer_internal_storage)
                            .withIdentifier(type)
                            .withSelectable(true));
                    break;
                default:
                    drawerItems.add(new PrimaryDrawerItem().withName(Path.getFileName(path))
                            .withTag(path)
                            .withIcon(R.drawable.ic_drawer_sdcard)
                            .withIdentifier(type)
                            .withSelectable(true));
                    break;
            }
        }
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.drawer_add_storage_provider)
                .withIcon(R.drawable.ic_drawer_add_storage_provider)
                .withIdentifier(DrawerItemUtils.DRAWER_ITEM_ADD_PROVIDER)
                .withSelectable(false));
        drawerItems.add(new DividerDrawerItem());
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.drawer_help_and_feedback)
                .withIdentifier(DrawerItemUtils.DRAWER_ITEM_HELP_FEEDBACK)
                .withSelectable(false));
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.drawer_github_repo)
                .withIdentifier(DrawerItemUtils.DRAWER_ITEM_GITHUB_REPO)
                .withSelectable(false));
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.drawer_settings)
                .withIdentifier(DrawerItemUtils.DRAWER_ITEM_SETTINGS)
                .withSelectable(false));
        mDrawerItems = drawerItems.toArray(new IDrawerItem[drawerItems.size()]);
    }

    public void onNavigationSelected(IDrawerItem drawerItem) {
        if(mDataListener != null)
            mDataListener.onNavigateTo(drawerItem);
    }

    public interface DataListener {
        void onDrawerItemsChanged(IDrawerItem[] drawerItems);
        void onNavigateTo(IDrawerItem drawerItem);
    }
}
