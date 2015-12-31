package org.cryse.unifystorage.explorer.utils;

import android.content.Context;

public class StorageDirectoryUtils {
    public static final int STORAGE_DIRECTORY_INTERNAL_STORAGE = 1000;
    public static final int STORAGE_DIRECTORY_EXTERNAl_STORAGE_START = 2001;

    public static int[] getStorageDirectoryTypes(Context context, String[] directories) {
        int[] directoryTypes = new int[directories.length];
        for (int i = 0; i < directories.length; i++) {
            if(i == 0) {
                directoryTypes[i] = STORAGE_DIRECTORY_INTERNAL_STORAGE;
            } else {
                directoryTypes[i] = STORAGE_DIRECTORY_EXTERNAl_STORAGE_START + i;
            }
        }
        return directoryTypes;
    }
}
