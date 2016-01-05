package org.cryse.unifystorage.explorer.ui;


import android.os.Bundle;

import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.ui.common.StorageProviderFragment;
import org.cryse.unifystorage.explorer.utils.StorageProviderBuilder;
import org.cryse.unifystorage.explorer.viewmodel.FileListViewModel;
import org.cryse.unifystorage.providers.dropbox.DropboxCredential;
import org.cryse.unifystorage.providers.dropbox.DropboxFile;
import org.cryse.unifystorage.providers.dropbox.DropboxStorageProvider;

public class DropboxStorageFragment extends StorageProviderFragment<
        DropboxFile,
        DropboxCredential,
        DropboxStorageProvider
        > {

    public static DropboxStorageFragment newInstance(DropboxCredential credential, int storageProviderRecordId) {
        DropboxStorageFragment fragment = new DropboxStorageFragment();
        Bundle args = new Bundle();
        args.putParcelable(DataContract.ARG_CREDENTIAL, credential);
        args.putInt(DataContract.ARG_STORAGE_PROVIDER_RECORD_ID, storageProviderRecordId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void readArguments() {
        Bundle args = getArguments();
        if (args.containsKey(DataContract.ARG_CREDENTIAL) && args.containsKey(DataContract.ARG_STORAGE_PROVIDER_RECORD_ID)) {
            mCredential = args.getParcelable(DataContract.ARG_CREDENTIAL);
            mStorageProviderRecordId = args.getInt(DataContract.ARG_STORAGE_PROVIDER_RECORD_ID);
        } else {
            throw new RuntimeException("Invalid credential.");
        }
    }

    @Override
    protected String getLogTag() {
        return DropboxStorageFragment.class.getSimpleName();
    }

    @Override
    protected FileListViewModel<DropboxFile, DropboxCredential, DropboxStorageProvider> buildViewModel(DropboxCredential credential) {
        return new FileListViewModel<>(
                getContext(),
                credential,
                new StorageProviderBuilder<DropboxFile, DropboxCredential, DropboxStorageProvider>() {
                    @Override
                    public DropboxStorageProvider buildStorageProvider(DropboxCredential credential) {
                        return new DropboxStorageProvider(credential, DataContract.CONST_DROPBOX_CLIENT_IDENTIFIER);
                    }
                },
                this
        );
    }
}
