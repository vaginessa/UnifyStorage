package org.cryse.unifystorage.explorer.ui;


import android.os.Bundle;

import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.application.StorageProviderManager;
import org.cryse.unifystorage.explorer.model.StorageProviderRecord;
import org.cryse.unifystorage.explorer.ui.common.StorageProviderFragment;
import org.cryse.unifystorage.explorer.utils.StorageProviderBuilder;
import org.cryse.unifystorage.explorer.viewmodel.FileListViewModel;
import org.cryse.unifystorage.providers.localstorage.LocalStorageFile;
import org.cryse.unifystorage.providers.localstorage.LocalStorageProvider;
import org.cryse.unifystorage.providers.onedrive.OneDriveCredential;
import org.cryse.unifystorage.providers.onedrive.OneDriveFile;
import org.cryse.unifystorage.providers.onedrive.OneDriveStorageProvider;

public class OneDriveStorageFragment extends StorageProviderFragment<
        OneDriveFile,
        OneDriveCredential,
        OneDriveStorageProvider
        > {

    public static OneDriveStorageFragment newInstance(OneDriveCredential credential, int storageProviderRecordId) {
        OneDriveStorageFragment fragment = new OneDriveStorageFragment();
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
        return OneDriveStorageFragment.class.getSimpleName();
    }

    @Override
    protected Class<OneDriveFile> getRemoteFileClass() {
        return OneDriveFile.class;
    }

    @Override
    protected FileListViewModel<OneDriveFile, OneDriveCredential, OneDriveStorageProvider> buildViewModel(OneDriveCredential credential) {
        return new FileListViewModel<>(
                getContext(),
                mStorageProviderRecordId,
                credential,
                new StorageProviderBuilder<OneDriveFile, OneDriveCredential, OneDriveStorageProvider>() {
                    @Override
                    public OneDriveStorageProvider buildStorageProvider(OneDriveCredential credential) {

                        return (OneDriveStorageProvider) StorageProviderManager
                                .getInstance()
                                .createStorageProvider(
                                        getActivity(),
                                        mStorageProviderRecordId,
                                        credential,
                                        DataContract.CONST_ONEDRIVE_CLIENT_ID
                                );
                    }
                },
                this
        );
    }
}
