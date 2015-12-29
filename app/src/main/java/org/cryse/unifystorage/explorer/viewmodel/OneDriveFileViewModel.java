package org.cryse.unifystorage.explorer.viewmodel;

import android.content.Context;

import org.cryse.unifystorage.providers.localstorage.LocalStorageFile;
import org.cryse.unifystorage.providers.onedrive.OneDriveFile;
import org.cryse.unifystorage.providers.onedrive.OneDriveStorageProvider;

public class OneDriveFileViewModel extends RemoteFileViewModel<OneDriveFile> {
    public OneDriveFileViewModel(Context context, OneDriveFile file) {
        super(context, file);
    }
}