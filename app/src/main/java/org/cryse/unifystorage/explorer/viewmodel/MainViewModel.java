package org.cryse.unifystorage.explorer.viewmodel;

import android.content.Context;

import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.model.StorageProviderRecord;
import org.cryse.unifystorage.explorer.utils.DrawerItemUtils;
import org.cryse.unifystorage.providers.localstorage.utils.LocalStorageUtils;
import org.cryse.unifystorage.utils.Path;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.Sort;

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
        List<IDrawerItem> drawerItems = new ArrayList<>();


        // Firstly get all local storage devices:
        String[] externalStoragePaths = LocalStorageUtils.getStorageDirectories(mContext);
        int[] externalStorageTypes = DrawerItemUtils.getStorageDirectoryTypes(mContext, externalStoragePaths);

        // Secondly get all saved storage providers
        List<StorageProviderRecord> savedStorageProviders = readSavedStorageProviders();
        int otherStorageProvidersCount = savedStorageProviders.size();

        // Finally the const items count
        int constDrawerItemsCount = 5;
        mDrawerItems = new IDrawerItem[externalStoragePaths.length + otherStorageProvidersCount + constDrawerItemsCount];

        // First insert all local storage devices
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
        // Then the saved providers
        for (StorageProviderRecord record : savedStorageProviders) {
            drawerItems.add(new PrimaryDrawerItem().withName(record.getDisplayName())
                    .withTag(record)
                    .withIcon(R.drawable.ic_drawer_sdcard)
                    .withIdentifier(record.getId())
                    .withSelectable(true));
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

    public void onNavigationSelected(IDrawerItem drawerItem) {
        if(mDataListener != null)
            mDataListener.onNavigateTo(drawerItem);
    }

    private List<StorageProviderRecord> readSavedStorageProviders() {
        Realm realm = Realm.getInstance(mContext);
        List<StorageProviderRecord> records = realm.copyFromRealm(realm.allObjectsSorted(StorageProviderRecord.class, "sortKey", Sort.ASCENDING));
        realm.close();
        return records;
    }

    public void addNewProvider(String displayName, String userName, int providerType, String credentialData, String extraData) {
        Realm realm = Realm.getInstance(mContext);
        int id = getNextKey(realm);
        StorageProviderRecord newRecord = new StorageProviderRecord();
        newRecord.setId(id);
        newRecord.setDisplayName(displayName);
        newRecord.setUserName(userName);
        newRecord.setCredentialData(credentialData);
        newRecord.setProviderType(providerType);
        newRecord.setExtraData(extraData);
        newRecord.setSortKey(new Date().getTime());
        realm.beginTransaction();
        realm.copyToRealm(newRecord);
        realm.commitTransaction();
        realm.close();
        updateDrawerItems();
    }

    public int getNextKey(Realm realm) {
        int maxId;
        if(realm.where(StorageProviderRecord.class).max("id") == null) {
            maxId = DrawerItemUtils.STORAGE_PROVIDER_START;
        } else {
            maxId = realm.where(StorageProviderRecord.class).max("id").intValue() + 1;
        }
        return maxId;
    }

    public interface DataListener {
        void onDrawerItemsChanged(IDrawerItem[] drawerItems);
        void onNavigateTo(IDrawerItem drawerItem);
    }
}
