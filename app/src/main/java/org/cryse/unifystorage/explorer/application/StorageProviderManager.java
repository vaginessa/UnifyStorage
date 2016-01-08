package org.cryse.unifystorage.explorer.application;

import android.content.Context;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.data.StorageProviderDatabase;
import org.cryse.unifystorage.explorer.model.StorageProviderRecord;
import org.cryse.unifystorage.explorer.utils.DrawerItemUtils;
import org.cryse.unifystorage.explorer.utils.StorageProviderBuilder;
import org.cryse.unifystorage.providers.localstorage.utils.LocalStorageUtils;
import org.cryse.unifystorage.utils.Path;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class StorageProviderManager {
    private static StorageProviderManager instance;
    Map<Integer, StorageProvider> mStorageProviderMap = new Hashtable<>();
    private StorageProviderDatabase mStorageProviderDatabase;
    private CompositeSubscription mBuildProviderSubscriptions = new CompositeSubscription();

    public static void init(Context context) {
        if (instance == null) {
            synchronized(StorageProviderManager.class) {
                if (instance == null) {
                    instance = new StorageProviderManager(context);
                }
            }
        }
    }

    public static StorageProviderManager getInstance() {
        return instance;
    }

    protected StorageProviderManager(Context context) {
        mStorageProviderDatabase = new StorageProviderDatabase(context);
    }

    public void addStorageProviderRecord(String displayName, String userName, int providerType, String credentialData, String extraData) {
        mStorageProviderDatabase.addNewProvider(displayName, userName, providerType, credentialData, extraData);
    }

    public void removeStorageProviderRecord(int id) {
        mStorageProviderDatabase.removeProviderRecord(id);
    }

    public List<StorageProviderRecord> loadStorageProviderRecordsWithLocal(Context context) {
        String[] externalStoragePaths = LocalStorageUtils.getStorageDirectories(context);
        int[] externalStorageTypes = DrawerItemUtils.getStorageDirectoryTypes(context, externalStoragePaths);
        List<StorageProviderRecord> localList = new ArrayList<>();
        for(int i = 0; i < externalStoragePaths.length; i++) {
            StorageProviderRecord record = new StorageProviderRecord();
            switch (externalStorageTypes[i]) {
                case DrawerItemUtils.STORAGE_DIRECTORY_INTERNAL_STORAGE:
                    record.setDisplayName(context.getString(R.string.drawer_local_internal_storage));
                    break;
                default:
                    record.setDisplayName(Path.getFileName(externalStoragePaths[i]));
                    break;
            }



            record.setId(externalStorageTypes[i]);
            record.setUserName("");
            record.setCredentialData("");
            record.setProviderType(StorageProviderRecord.PROVIDER_LOCAL_STORAGE);
            record.setExtraData(externalStoragePaths[i]);
            record.setSortKey(externalStorageTypes[i]);
            record.setUuid("");
            localList.add(record);
        }
        localList.addAll(loadStorageProviderRecords());
        return localList;
    }

    public List<StorageProviderRecord> loadStorageProviderRecords() {
        return mStorageProviderDatabase.getSavedStorageProviders();
    }

    public <RF extends RemoteFile, CR extends Credential, SP extends StorageProvider<RF, CR>>
    void loadStorageProvider(
            final int id,
            final StorageProviderBuilder<RF, CR, SP> builder,
            final CR credential,
            final OnLoadStorageProviderCallback<RF, CR, SP> callback
    ) {
        if (mStorageProviderMap.containsKey(id)) {
            callback.onSuccess((SP) mStorageProviderMap.get(id));
        } else {
            Subscription subscription = Observable.create(new Observable.OnSubscribe<SP>() {
                @Override
                public void call(Subscriber<? super SP> subscriber) {
                    try {
                        SP storageProvider = builder.buildStorageProvider(credential);
                        subscriber.onNext(storageProvider);
                        subscriber.onCompleted();
                    } catch (Throwable throwable) {
                        subscriber.onError(throwable);
                    }
                }
            })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<SP>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            callback.onFailure(e);
                        }

                        @Override
                        public void onNext(SP storageProvider) {
                            if (storageProvider.shouldRefreshCredential() && id >= 0) {
                                StorageProviderRecord record = mStorageProviderDatabase.getSavedStorageProvider(id);
                                CR newCredential = storageProvider.getRefreshedCredential();
                                if (mStorageProviderDatabase != null) {
                                    record.setCredentialData(newCredential.persist());
                                    mStorageProviderDatabase.updateStorageProviderRecord(record);
                                }
                            }
                            mStorageProviderMap.put(id, storageProvider);
                            callback.onSuccess(storageProvider);
                        }
                    });
            mBuildProviderSubscriptions.add(subscription);
        }

    }

    public void destroy() {
        if(mBuildProviderSubscriptions.hasSubscriptions() && !mBuildProviderSubscriptions.isUnsubscribed())
            mBuildProviderSubscriptions.unsubscribe();
        mStorageProviderMap.clear();
        mStorageProviderDatabase.destroy();
    }

    public interface OnLoadStorageProviderCallback<RF extends RemoteFile, CR extends Credential, SP extends StorageProvider<RF, CR>> {
        void onSuccess(SP storageProvider);

        void onFailure(Throwable error);
    }
}
