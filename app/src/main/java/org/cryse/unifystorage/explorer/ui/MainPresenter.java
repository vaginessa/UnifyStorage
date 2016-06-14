package org.cryse.unifystorage.explorer.ui;

import android.content.Context;

import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.application.StorageProviderManager;
import org.cryse.unifystorage.explorer.model.StorageProviderRecord;
import org.cryse.unifystorage.explorer.utils.DrawerItemUtils;

import java.util.ArrayList;
import java.util.List;

public class MainPresenter implements MainContract.Presenter {
    private Context mContext;
    private MainContract.View mView;
    private IDrawerItem[] mDrawerItems;

    public MainPresenter(Context context, MainContract.View view) {
        mContext = context;
        mView = view;
    }

    @Override
    public void updateDrawerItems() {
        buildDrawerItems();
        mView.onDrawerItemsChanged(mDrawerItems);
        /*if(mView != null)
            mView.onDrawerItemsChanged(mDrawerItems, currentSelectionIdentifier);*/
    }

    private void buildDrawerItems() {
        List<IDrawerItem> drawerItems = new ArrayList<>();
        List<StorageProviderRecord> storageProviderRecords = StorageProviderManager.instance().loadStorageProviderRecordsWithLocal(mContext);
        for(StorageProviderRecord record : storageProviderRecords) {
            PrimaryDrawerItem item = new PrimaryDrawerItem();
            item.withName(record.getDisplayName()).withSelectable(false).withIdentifier(record.getId());
            switch (record.getProviderType()) {
                case StorageProviderRecord.PROVIDER_LOCAL_STORAGE:
                    if(record.getId() == DrawerItemUtils.STORAGE_DIRECTORY_INTERNAL_STORAGE) {
                        item.withTag(record.getExtraData())
                                .withIcon(R.drawable.ic_drawer_internal_storage);
                    } else if(record.getId() <= DrawerItemUtils.STORAGE_DIRECTORY_EXTERNAl_STORAGE_START) {
                        item.withTag(record.getExtraData())
                                .withIcon(R.drawable.ic_drawer_sdcard);
                    } else {
                        item.withTag(record)
                                .withIcon(R.drawable.ic_format_folder);
                    }
                    break;
                case StorageProviderRecord.PROVIDER_DROPBOX:
                    item.withTag(record)
                            .withIcon(R.drawable.ic_icon_dropbox);
                    break;
                case StorageProviderRecord.PROVIDER_ONE_DRIVE:
                    item.withTag(record)
                            .withIcon(R.drawable.ic_icon_onedrive);
                    break;
                case StorageProviderRecord.PROVIDER_GOOGLE_DRIVE:
                    item.withTag(record)
                            .withIcon(R.drawable.ic_icon_googledrive);
                    break;
                default:
                    item.withTag(record)
                            .withIcon(R.drawable.ic_drawer_cloud);
            }
            drawerItems.add(item);
        }

        // Finally const items
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

    @Override
    public void onNavigationSelected(IDrawerItem drawerItem) {
        if(mView != null)
            mView.onNavigateTo(drawerItem);
    }

    @Override
    public void addNewProvider(String displayName, String userName, int providerType, String credentialData, String extraData) {
        StorageProviderManager.instance().addStorageProviderRecord(displayName, userName, providerType, credentialData, extraData);
    }

    @Override
    public void start() {

    }

    @Override
    public void destroy() {

    }
}
