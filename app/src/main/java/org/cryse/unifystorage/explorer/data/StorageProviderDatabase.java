package org.cryse.unifystorage.explorer.data;

import android.content.Context;

import org.cryse.unifystorage.explorer.model.StorageProviderRecord;
import org.cryse.unifystorage.explorer.utils.DrawerItemUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class StorageProviderDatabase {
    private Context mContext;
    private Realm mRealm;

    public StorageProviderDatabase(Context context) {
        this.mContext = context;
        this.mRealm = Realm.getInstance(context);
    }

    public void updateStorageProviderRecord(StorageProviderRecord record) {
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(record);
        mRealm.commitTransaction();
    }

    public StorageProviderRecord getSavedStorageProvider(int id) {
        RealmQuery<StorageProviderRecord> query = mRealm.where(StorageProviderRecord.class);
        query.equalTo("id", id);
        RealmResults<StorageProviderRecord> results = query.findAll();
        if(results.size() > 0) {
            return mRealm.copyFromRealm(results.get(0));
        } else {
            return null;
        }
    }

    public List<StorageProviderRecord> getSavedStorageProviders() {
        return mRealm.copyFromRealm(mRealm.allObjectsSorted(StorageProviderRecord.class, "sortKey", Sort.ASCENDING));
    }

    public void addNewProvider(String displayName, String userName, int providerType, String credentialData, String extraData) {
        int id = getNextKey(mRealm);
        StorageProviderRecord newRecord = new StorageProviderRecord();
        newRecord.setId(id);
        newRecord.setDisplayName(displayName);
        newRecord.setUserName(userName);
        newRecord.setCredentialData(credentialData);
        newRecord.setProviderType(providerType);
        newRecord.setExtraData(extraData);
        newRecord.setSortKey(new Date().getTime());
        newRecord.setUuid(UUID.randomUUID().toString());
        mRealm.beginTransaction();
        mRealm.copyToRealm(newRecord);
        mRealm.commitTransaction();
        // updateDrawerItems();
    }

    public void destroy() {
        if(mRealm != null && !mRealm.isClosed()) {
            mRealm.close();
        }
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
}
