package org.cryse.unifystorage.explorer.viewmodel;

import android.content.Context;

import org.cryse.unifystorage.providers.localstorage.LocalStorageFile;
import org.cryse.unifystorage.providers.localstorage.LocalStorageProvider;

public class LocalStorageFileViewModel extends RemoteFileViewModel<LocalStorageFile> {
    public LocalStorageFileViewModel(Context context, LocalStorageFile file) {
        super(context, file);
    }
}
