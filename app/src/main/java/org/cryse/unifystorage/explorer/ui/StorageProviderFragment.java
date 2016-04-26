package org.cryse.unifystorage.explorer.ui;

import android.os.Bundle;

import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.application.StorageProviderManager;
import org.cryse.unifystorage.explorer.ui.common.AbstractStorageProviderFragment;
import org.cryse.unifystorage.explorer.utils.StorageProviderBuilder;
import org.cryse.unifystorage.explorer.viewmodel.FileListViewModel;

public class StorageProviderFragment extends AbstractStorageProviderFragment {
    public static StorageProviderFragment newInstance(Credential credential, int storageProviderRecordId, String...extraArgs) {
        StorageProviderFragment fragment = new StorageProviderFragment();
        Bundle args = new Bundle();
        args.putParcelable(DataContract.ARG_CREDENTIAL, credential);
        args.putInt(DataContract.ARG_STORAGE_PROVIDER_RECORD_ID, storageProviderRecordId);
        args.putStringArray(DataContract.ARG_EXTRAS, extraArgs);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void readArguments() {
        Bundle args = getArguments();
        if (args.containsKey(DataContract.ARG_CREDENTIAL) && args.containsKey(DataContract.ARG_STORAGE_PROVIDER_RECORD_ID)) {
            mCredential = args.getParcelable(DataContract.ARG_CREDENTIAL);
            mStorageProviderRecordId = args.getInt(DataContract.ARG_STORAGE_PROVIDER_RECORD_ID);
            mExtras = args.getStringArray(DataContract.ARG_EXTRAS);
        } else {
            throw new RuntimeException("Invalid credential.");
        }
    }

    @Override
    protected String getLogTag() {
        return StorageProviderFragment.class.getSimpleName();
    }

    @Override
    protected FileListViewModel buildViewModel(Credential credential) {
        return new FileListViewModel(
                getContext(),
                mStorageProviderRecordId,
                credential,
                new StorageProviderBuilder() {
                    @Override
                    public StorageProvider buildStorageProvider(Credential credential) {
                        return StorageProviderManager
                                .getInstance()
                                .createStorageProvider(
                                        getActivity(),
                                        mStorageProviderRecordId,
                                        credential,
                                        mExtras[0]
                                );
                    }
                },
                this
        );
    }
}
