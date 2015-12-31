package org.cryse.unifystorage.providers.localstorage.utils;

import android.content.Context;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LocalStorageUtils {


    public static String[] getStorageDirectories(Context context) {
        StorageManager storageManager = (StorageManager)context
                .getSystemService(context.STORAGE_SERVICE);
        Method methodGetPaths;
        List<String> directories = new ArrayList<>();
        try {
            methodGetPaths = storageManager.getClass()
                    .getMethod("getVolumePaths");
            String[] paths = (String[]) methodGetPaths.invoke(storageManager);
            for(String path : paths) {
                File file = new File(path);
                if(file.exists() && file.isDirectory() && file.canRead())
                    directories.add(path);
            }
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Log.e("LocalStorageUtils", e.getMessage(), e);
        } finally {
            return directories.toArray(new String[directories.size()]);
        }
    }
}
