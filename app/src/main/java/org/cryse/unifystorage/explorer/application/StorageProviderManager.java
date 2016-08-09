package org.cryse.unifystorage.explorer.application;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.data.UnifyStorageDatabase;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.model.StorageProviderRecord;
import org.cryse.unifystorage.explorer.model.StorageProviderType;
import org.cryse.unifystorage.explorer.model.StorageUriRecord;
import org.cryse.unifystorage.explorer.utils.DrawerItemUtils;
import org.cryse.unifystorage.providers.dropbox.DropboxCredential;
import org.cryse.unifystorage.providers.dropbox.DropboxStorageProvider;
import org.cryse.unifystorage.providers.localstorage.LocalStorageProvider;
import org.cryse.unifystorage.providers.localstorage.utils.LocalStorageUtils;
import org.cryse.unifystorage.providers.onedrive.OneDriveCredential;
import org.cryse.unifystorage.providers.onedrive.OneDriveStorageProvider;
import org.cryse.unifystorage.utils.Path;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class StorageProviderManager {
    private static StorageProviderManager instance;
    private HttpLoggingInterceptor mLoggingInterceptor;
    private OkHttpClient mOkHttpClient;
    private Handler mHandler;
    private UnifyStorageDatabase mUnifyStorageDatabase;

    public static void init(Context context) {
        if (instance == null) {
            synchronized(StorageProviderManager.class) {
                if (instance == null) {
                    instance = new StorageProviderManager(context);
                }
            }
        }
    }

    public static StorageProviderManager instance() {
        return instance;
    }

    protected StorageProviderManager(Context context) {
        mHandler = new Handler(context.getMainLooper());
        mUnifyStorageDatabase = UnifyStorageDatabase.instance();
        mLoggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(mLoggingInterceptor)
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
    }

    public void addStorageProviderRecord(String displayName, String userName, int providerType, String credentialData, String extraData) {
        mUnifyStorageDatabase.addNewProvider(displayName, userName, providerType, credentialData, extraData);
    }

    public void updateStorageProviderRecord(final StorageProviderRecord record, boolean ensureOnMainThread) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mUnifyStorageDatabase.updateStorageProviderRecord(record);
            }
        };
        if(ensureOnMainThread) mHandler.post(runnable);
        else runnable.run();
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
            record.setProviderType(StorageProviderType.LOCAL_STORAGE.getValue());
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

    public StorageProvider createStorageProvider(Context context, StorageProviderInfo info) {
        return createStorageProvider(context, info.getStorageProviderId(), info.getCredential(), info.getExtras());
    }

    public StorageProvider createStorageProvider(Context context, int id, Credential credential, Object...extra) {
        if(id < 0) {
            LocalStorageProvider localStorageProvider = new LocalStorageProvider(context, (String)extra[0]);
            if(LocalStorageUtils.isOnSdcard(context, (String)extra[0])) {
                StorageUriRecord uriRecord = UnifyStorageDatabase.instance().getStorageUriRecord(LocalStorageUtils.getSdcardDirectory(context, (String) extra[0]));
                if (uriRecord == null) {
                    // requestSdcardUri();
                } else {
                    Uri sdcardUri = Uri.parse(uriRecord.getUriData());
                    localStorageProvider.setSdcardUri(sdcardUri);
                }
            }
            return localStorageProvider;
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
            StorageProviderType type = StorageProviderType.fromInt(record.getProviderType());
            switch (type) {
                case LOCAL_STORAGE:

                    LocalStorageProvider localStorageProvider = new LocalStorageProvider(context, (String)extra[0]);
                    if(LocalStorageUtils.isOnSdcard(context, (String)extra[0])) {
                        StorageUriRecord uriRecord = UnifyStorageDatabase.instance().getStorageUriRecord(LocalStorageUtils.getSdcardDirectory(context, (String) extra[0]));
                        if (uriRecord == null) {
                            // requestSdcardUri();
                        } else {
                            Uri sdcardUri = Uri.parse(uriRecord.getUriData());
                            localStorageProvider.setSdcardUri(sdcardUri);
                        }
                    }
                    storageProvider = localStorageProvider;
                    break;
                case DROPBOX:
                    storageProvider = new DropboxStorageProvider(mOkHttpClient, (DropboxCredential) credential, (String)extra[0]);
                    break;
                case ONE_DRIVE:
                    storageProvider = new OneDriveStorageProvider(mOkHttpClient, (OneDriveCredential) credential, (String)extra[0]);
                    break;
                case GOOGLE_DRIVE:
                    break;
            }
            return storageProvider;
        }
    }

    private void destroyManager() {
    }

    public static void destroy() {
        if(instance != null)
            instance.destroyManager();
    }
}
