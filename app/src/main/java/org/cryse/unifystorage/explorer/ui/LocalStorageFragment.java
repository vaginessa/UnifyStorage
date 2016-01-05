package org.cryse.unifystorage.explorer.ui;


import android.os.Bundle;

import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.ui.common.StorageProviderFragment;
import org.cryse.unifystorage.explorer.utils.StorageProviderBuilder;
import org.cryse.unifystorage.explorer.viewmodel.FileListViewModel;
import org.cryse.unifystorage.providers.localstorage.LocalCredential;
import org.cryse.unifystorage.providers.localstorage.LocalStorageFile;
import org.cryse.unifystorage.providers.localstorage.LocalStorageProvider;

public class LocalStorageFragment extends StorageProviderFragment<
        LocalStorageFile,
        LocalCredential,
        LocalStorageProvider
        > {
    protected String mStartPath;

    public static LocalStorageFragment newInstance(String startPath, int storageProviderRecordId) {
        LocalStorageFragment fragment = new LocalStorageFragment();
        Bundle args = new Bundle();
        args.putString(DataContract.ARG_LOCAL_PATH, startPath);
        args.putInt(DataContract.ARG_STORAGE_PROVIDER_RECORD_ID, storageProviderRecordId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void readArguments() {
        Bundle args = getArguments();
        if (args.containsKey(DataContract.ARG_LOCAL_PATH)) {
            mStartPath = args.getString(DataContract.ARG_LOCAL_PATH);
        } else {
            throw new RuntimeException("Invalid path.");
        }
        if(args.containsKey(DataContract.ARG_STORAGE_PROVIDER_RECORD_ID))
            mStorageProviderRecordId = args.getInt(DataContract.ARG_STORAGE_PROVIDER_RECORD_ID);
    }

    @Override
    protected String getLogTag() {
        return LocalStorageFragment.class.getSimpleName();
    }

    @Override
    protected FileListViewModel<LocalStorageFile, LocalCredential, LocalStorageProvider> buildViewModel(LocalCredential credential) {
        return new FileListViewModel<>(
                getContext(),
                credential,
                new StorageProviderBuilder<LocalStorageFile, LocalCredential, LocalStorageProvider>() {
                    @Override
                    public LocalStorageProvider buildStorageProvider(LocalCredential credential) {
                        return new LocalStorageProvider(mStartPath);
                    }
                },
                this
        );
    }
}
