package org.cryse.unifystorage.explorer.ui;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.data.UnifyStorageDatabase;
import org.cryse.unifystorage.explorer.model.StorageUriRecord;
import org.cryse.unifystorage.explorer.ui.common.StorageProviderFragment;
import org.cryse.unifystorage.explorer.utils.StorageProviderBuilder;
import org.cryse.unifystorage.explorer.viewmodel.FileListViewModel;
import org.cryse.unifystorage.providers.localstorage.LocalCredential;
import org.cryse.unifystorage.providers.localstorage.LocalStorageFile;
import org.cryse.unifystorage.providers.localstorage.LocalStorageProvider;
import org.cryse.unifystorage.providers.localstorage.utils.LocalStorageUtils;

public class LocalStorageFragment extends StorageProviderFragment<
        LocalStorageFile,
        LocalCredential,
        LocalStorageProvider
        > {
    public static final int RC_SDCARD_URI = 102;
    protected String mStartPath;
    private Uri mSdcardUri;
    protected boolean mOnSdCard;

    public static LocalStorageFragment newInstance(String startPath, int storageProviderRecordId) {
        LocalStorageFragment fragment = new LocalStorageFragment();
        Bundle args = new Bundle();
        args.putString(DataContract.ARG_LOCAL_PATH, startPath);
        args.putInt(DataContract.ARG_STORAGE_PROVIDER_RECORD_ID, storageProviderRecordId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public void onStorageProviderReady() {
        super.onStorageProviderReady();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(LocalStorageUtils.isOnSdcard(getContext(), mStartPath)) {
                mOnSdCard = true;
                // TODO: try to request sdcard uri here!
                StorageUriRecord record = UnifyStorageDatabase.getInstance().getStorageUriRecord(LocalStorageUtils.getSdcardDirectory(getContext(), mStartPath));
                if(record == null) {
                    requestSdcardUri();
                } else {
                    mSdcardUri = Uri.parse(record.getUriData());
                    mViewModel.getStorageProvider().getStorageProvider().setSdcardUri(mSdcardUri);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestSdcardUri() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, RC_SDCARD_URI);
    }

    @Override
    protected Class<LocalStorageFile> getRemoteFileClass() {
        return LocalStorageFile.class;
    }

    @Override
    protected String getLogTag() {
        return LocalStorageFragment.class.getSimpleName();
    }

    @Override
    protected FileListViewModel<LocalStorageFile, LocalCredential, LocalStorageProvider> buildViewModel(LocalCredential credential) {
        return new FileListViewModel<>(
                getContext(),
                mStorageProviderRecordId,
                credential,
                new StorageProviderBuilder<LocalStorageFile, LocalCredential, LocalStorageProvider>() {
                    @Override
                    public LocalStorageProvider buildStorageProvider(LocalCredential credential) {
                        return new LocalStorageProvider(getContext(), mStartPath);
                    }
                },
                this
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SDCARD_URI && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(resultCode == Activity.RESULT_OK) {
                mSdcardUri = data.getData();
                getContext().getContentResolver().takePersistableUriPermission(mSdcardUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                StorageUriRecord record = new StorageUriRecord();
                record.setPath(LocalStorageUtils.getSdcardDirectory(getContext(), mStartPath));
                record.setUriData(mSdcardUri.toString());
                UnifyStorageDatabase.getInstance().saveStorageUriRecord(record);
                mViewModel.getStorageProvider().getStorageProvider().setSdcardUri(mSdcardUri);
            } else {
                // TODO: show error here;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
