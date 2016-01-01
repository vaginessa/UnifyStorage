package org.cryse.unifystorage.explorer.ui;


import android.os.Bundle;

import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.ui.common.StorageProviderFragment;
import org.cryse.unifystorage.explorer.utils.StorageProviderBuilder;
import org.cryse.unifystorage.explorer.viewmodel.FileListViewModel;
import org.cryse.unifystorage.providers.localstorage.LocalStorageFile;
import org.cryse.unifystorage.providers.localstorage.LocalStorageProvider;

public class LocalStorageFragment extends StorageProviderFragment<
        LocalStorageFile,
        LocalStorageProvider,
        Credential
        > {
    protected String mStartPath;

    public static LocalStorageFragment newInstance(String startPath) {
        LocalStorageFragment fragment = new LocalStorageFragment();
        Bundle args = new Bundle();
        args.putString(DataContract.ARG_LOCAL_PATH, startPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args.containsKey(DataContract.ARG_LOCAL_PATH)) {
            mStartPath = args.getString(DataContract.ARG_LOCAL_PATH);
        } else {
            mStartPath = "/sdcard";
        }
        // LocalStorageProvider do not need credential.
        mViewModel = buildViewModel(null);
    }

    @Override
    protected FileListViewModel<LocalStorageFile, LocalStorageProvider, Credential> buildViewModel(Credential credential) {
        return new FileListViewModel<>(
                getContext(),
                credential,
                new StorageProviderBuilder<LocalStorageFile, LocalStorageProvider, Credential>() {
                    @Override
                    public LocalStorageProvider buildStorageProvider(Credential credential) {
                        return new LocalStorageProvider(mStartPath);
                    }
                },
                this
        );
    }
}
