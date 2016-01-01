package org.cryse.unifystorage.explorer.utils;

import android.content.Context;

public class DrawerItemUtils {
    public static final int DRAWER_ITEM_NONE = 15;
    public static final int STORAGE_DIRECTORY_INTERNAL_STORAGE = 1000;
    public static final int STORAGE_DIRECTORY_EXTERNAl_STORAGE_START = 2001;
    public static final int DRAWER_ITEM_ADD_PROVIDER = 2000;
    public static final int DRAWER_ITEM_HELP_FEEDBACK = 6001;
    public static final int DRAWER_ITEM_GITHUB_REPO = 6002;
    public static final int DRAWER_ITEM_SETTINGS = 6003;
    public static final int STORAGE_PROVIDER_START = 30000;

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
