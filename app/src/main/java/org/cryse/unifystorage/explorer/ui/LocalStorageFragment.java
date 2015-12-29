package org.cryse.unifystorage.explorer.ui;


import android.os.Bundle;

import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.ui.common.StorageProviderFragment;
import org.cryse.unifystorage.providers.localstorage.LocalStorageFile;
import org.cryse.unifystorage.providers.localstorage.LocalStorageProvider;

public class LocalStorageFragment extends StorageProviderFragment<
        LocalStorageFile,
        LocalStorageProvider
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
        if(args.containsKey(DataContract.ARG_LOCAL_PATH)) {
            mStartPath = args.getString(DataContract.ARG_LOCAL_PATH);
        } else {
            mStartPath = "/sdcard";
        }
        mStorageProvider = buildStorageProvider(mCredential);
    }

    @Override
    protected LocalStorageProvider buildStorageProvider(Credential credential) {
        return new LocalStorageProvider(mStartPath);
    }
}
