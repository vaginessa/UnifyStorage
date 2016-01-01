package org.cryse.unifystorage.explorer.ui;


import android.os.Bundle;

import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.DataContract;
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
        OneDriveStorageProvider,
        OneDriveCredential
        > {
    private OneDriveCredential mCredential;

    public static OneDriveStorageFragment newInstance(OneDriveCredential credential) {
        OneDriveStorageFragment fragment = new OneDriveStorageFragment();
        Bundle args = new Bundle();
        args.putParcelable(DataContract.ARG_CREDENTIAL, credential);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args.containsKey(DataContract.ARG_CREDENTIAL)) {
            mCredential = args.getParcelable(DataContract.ARG_CREDENTIAL);
        } else {
            throw new RuntimeException("Invalid credential.");
        }
        // LocalStorageProvider do not need credential.
        mViewModel = buildViewModel(mCredential);
    }

    @Override
    protected FileListViewModel<OneDriveFile, OneDriveStorageProvider, OneDriveCredential> buildViewModel(OneDriveCredential credential) {
        return new FileListViewModel<>(
                getContext(),
                credential,
                new StorageProviderBuilder<OneDriveFile, OneDriveStorageProvider, OneDriveCredential>() {
                    @Override
                    public OneDriveStorageProvider buildStorageProvider(OneDriveCredential credential) {
                        return new OneDriveStorageProvider(getActivity(), DataContract.CONST_ONEDRIVE_CLIENT_ID, credential);
                    }
                },
                this
        );
    }
}
