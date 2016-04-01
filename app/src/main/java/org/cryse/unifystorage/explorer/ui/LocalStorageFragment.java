package org.cryse.unifystorage.explorer.ui;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileObserver;
import android.text.TextUtils;
import android.util.Log;

import com.owncloud.android.utils.RecursiveFileObserver;

import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.data.UnifyStorageDatabase;
import org.cryse.unifystorage.explorer.event.FileDeleteEvent;
import org.cryse.unifystorage.explorer.model.StorageUriRecord;
import org.cryse.unifystorage.providers.localstorage.LocalStorageProvider;
import org.cryse.unifystorage.providers.localstorage.utils.LocalStorageUtils;
import org.cryse.unifystorage.utils.Path;

public class LocalStorageFragment extends StorageProviderFragment {
    public static final int RC_SDCARD_URI = 102;
    protected String mStartPath;
    private Uri mSdcardUri;
    protected boolean mOnSdCard;
    private FileObserver mFileObserver;

    public static LocalStorageFragment newInstance(int storageProviderRecordId, String...extraArgs) {
        LocalStorageFragment fragment = new LocalStorageFragment();
        Bundle args = new Bundle();
        args.putParcelable(DataContract.ARG_CREDENTIAL, null);
        args.putInt(DataContract.ARG_STORAGE_PROVIDER_RECORD_ID, storageProviderRecordId);
        args.putStringArray(DataContract.ARG_EXTRAS, extraArgs);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void readArguments() {
        super.readArguments();
        this.mStartPath = mExtras[0];
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
                    ((LocalStorageProvider)mViewModel.getStorageProvider().getStorageProvider()).setSdcardUri(mSdcardUri);
                }
            }
        }
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
        }
        mFileObserver = new RecursiveFileObserver(mStartPath, 2) { // set up a file observer to watch this directory on sd card
            @Override
            public void onEvent(int event, String file) {
                if ((event & CLOSE_NOWRITE) != 0 || (event & OPEN) != 0) return;
                if (mViewModel.getDirectory() != null && !TextUtils.isEmpty(file)) {
                    String currentDirectoryPath = mViewModel.getDirectory().directory.getPath();
                    if(Path.isEqualOrDirectChild(currentDirectoryPath, file)) {
                        Log.e("FILE_EQUAL",
                                String.format(
                                        "onEvent:\n\t\tEvent: %08X\n\t\tParent: %s\n\t\tChild: %s",
                                        event,
                                        mViewModel.getDirectory().directory.getPath(),
                                        file
                                )
                        );
                        if ((event & MOVE_SELF) != 0) {
                            mViewModel.loadFiles(mViewModel.getDirectory().directory);
                            Log.e(getLogTag(), String.format("Directory MOVE_SELF: %s", file));
                        }
                        if ((event & MOVED_FROM) != 0) {
                            mViewModel.loadFiles(mViewModel.getDirectory().directory);
                            Log.e(getLogTag(), String.format("Directory MOVED_FROM: %s", file));
                        }
                        if ((event & DELETE) != 0) {
                            mViewModel.loadFiles(mViewModel.getDirectory().directory);
                            Log.e(getLogTag(), String.format("Directory DELETE: %s", file));
                        }
                        if ((event & DELETE_SELF) != 0) {
                        }
                        if ((event & MODIFY) != 0) {
                        }
                        if ((event & CREATE) != 0) {
                            mViewModel.loadFiles(mViewModel.getDirectory().directory);
                            Log.e(getLogTag(), String.format("Directory CREATE: %s", file));
                        }
                    }
                }
            }
        };
        mFileObserver.startWatching(); //START OBSERVING
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestSdcardUri() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, RC_SDCARD_URI);
    }

    @Override
    protected String getLogTag() {
        return LocalStorageFragment.class.getSimpleName();
    }

    @Override
    protected void onFileDeleteEvent(FileDeleteEvent fileDeleteEvent) {
        // super.onFileDeleteEvent(fileDeleteEvent);
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
                ((LocalStorageProvider)mViewModel.getStorageProvider().getStorageProvider()).setSdcardUri(mSdcardUri);
            } else {
                // TODO: show error here;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mFileObserver != null)
            mFileObserver.stopWatching();
    }
}
