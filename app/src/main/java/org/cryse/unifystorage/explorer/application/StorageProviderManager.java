package org.cryse.unifystorage.explorer.application;

import android.content.Context;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.data.UnifyStorageDatabase;
import org.cryse.unifystorage.explorer.model.StorageProviderRecord;
import org.cryse.unifystorage.explorer.utils.DrawerItemUtils;
import org.cryse.unifystorage.explorer.utils.StorageProviderBuilder;
import org.cryse.unifystorage.providers.dropbox.DropboxCredential;
import org.cryse.unifystorage.providers.dropbox.DropboxStorageProvider;
import org.cryse.unifystorage.providers.localstorage.LocalStorageProvider;
import org.cryse.unifystorage.providers.localstorage.utils.LocalStorageUtils;
import org.cryse.unifystorage.providers.onedrive.OneDriveConst;
import org.cryse.unifystorage.providers.onedrive.OneDriveCredential;
import org.cryse.unifystorage.providers.onedrive.OneDriveStorageProvider;
import org.cryse.unifystorage.utils.Path;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class StorageProviderManager {
    private static StorageProviderManager instance;
    HttpLoggingInterceptor mLoggingInterceptor;
    OkHttpClient mOkHttpClient;


    Map<Integer, StorageProvider> mStorageProviderMap = new Hashtable<>();
    private UnifyStorageDatabase mUnifyStorageDatabase;
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
        mUnifyStorageDatabase = UnifyStorageDatabase.getInstance();
        mLoggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(mLoggingInterceptor)
                .build();
    }

    public void addStorageProviderRecord(String displayName, String userName, int providerType, String credentialData, String extraData) {
        mUnifyStorageDatabase.addNewProvider(displayName, userName, providerType, credentialData, extraData);
    }

    public void removeStorageProviderRecord(int id) {
        mUnifyStorageDatabase.removeProviderRecord(id);
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

    public StorageProviderRecord loadStorageProviderRecord(int id) {
        return mUnifyStorageDatabase.getSavedStorageProvider(id);
    }

    public List<StorageProviderRecord> loadStorageProviderRecords() {
        return mUnifyStorageDatabase.getSavedStorageProviders();
    }

    public <RF extends RemoteFile, CR extends Credential, SP extends StorageProvider<RF, CR>>
    SP loadStorageProvider(final int id) {
        if (mStorageProviderMap.containsKey(id)) {
            return (SP) mStorageProviderMap.get(id);
        } else {
            return null;
        }
    }

    public StorageProvider createStorageProvider(Context context, int id, Credential credential, Object...extra) {
        if(id < 0) {
            return new LocalStorageProvider(context, (String)extra[0]);
            // Local Storage Provider
            /*if(id == DrawerItemUtils.STORAGE_DIRECTORY_INTERNAL_STORAGE) {
                // Internal Storage
                return new LocalStorageProvider(context, (String)extra[0]);
            } else if(id < DrawerItemUtils.STORAGE_DIRECTORY_EXTERNAl_STORAGE_START) {
                // External Storage or Other Local Storage
                return new LocalStorageProvider(context, (String)extra[0]);
            }*/
        } else {
            // Other StorageProvider
            StorageProvider storageProvider = null;
            StorageProviderRecord record = mUnifyStorageDatabase.getSavedStorageProvider(id);
            switch (record.getProviderType()) {
                case StorageProviderRecord.PROVIDER_DROPBOX:
                    storageProvider = new DropboxStorageProvider(mOkHttpClient, (DropboxCredential) credential, (String)extra[0]);
                    break;
                case StorageProviderRecord.PROVIDER_ONE_DRIVE:
                    storageProvider = new OneDriveStorageProvider(mOkHttpClient, (OneDriveCredential) credential, (String)extra[0]);
                    break;
                case StorageProviderRecord.PROVIDER_GOOGLE_DRIVE:
                    break;
            }
            return storageProvider;
        }
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
                            /*if (storageProvider.shouldRefreshCredential() && id >= 0) {
                                StorageProviderRecord record = mUnifyStorageDatabase.getSavedStorageProvider(id);
                                CR newCredential = storageProvider.getRefreshedCredential();
                                if (mUnifyStorageDatabase != null) {
                                    record.setCredentialData(newCredential.persist());
                                    mUnifyStorageDatabase.updateStorageProviderRecord(record);
                                }
                            }*/
                            mStorageProviderMap.put(id, storageProvider);
                            callback.onSuccess(storageProvider);
                        }
                    });
            mBuildProviderSubscriptions.add(subscription);
        }

    }

    private void destroyManager() {
        if(mBuildProviderSubscriptions.hasSubscriptions() && !mBuildProviderSubscriptions.isUnsubscribed())
            mBuildProviderSubscriptions.unsubscribe();
        mStorageProviderMap.clear();
    }

    public static void destroy() {
        if(instance != null)
            instance.destroyManager();
    }

    public interface OnLoadStorageProviderCallback<RF extends RemoteFile, CR extends Credential, SP extends StorageProvider<RF, CR>> {
        void onSuccess(SP storageProvider);

        void onFailure(Throwable error);
    }
}
