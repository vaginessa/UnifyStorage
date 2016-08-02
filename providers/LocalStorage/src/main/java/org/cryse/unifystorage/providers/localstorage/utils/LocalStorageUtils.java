package org.cryse.unifystorage.providers.localstorage.utils;

import android.content.Context;
import android.os.storage.StorageManager;
import android.util.Log;

import org.cryse.unifystorage.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class LocalStorageUtils {


    public static String[] getStorageDirectories(Context context) {
        List<String> dirs = getStorageDirectoriesList(context);
        return dirs.toArray(new String[dirs.size()]);
    }

    private static List<String> getStorageDirectoriesList(Context context) {
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
            return directories;
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Log.e("LocalStorageUtils", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public static boolean isOnSdcard(Context context, String path) {
        List<String> dirs = getStorageDirectoriesList(context);
        for (int i = 0; i < dirs.size(); i++) {
            String dirPath = dirs.get(i);
            if(i != 0 && path.startsWith(dirPath))
                return true;
        }
        return false;
    }

    public static String getSdcardDirectory(Context context, String path) {
        List<String> dirs = getStorageDirectoriesList(context);
        for (int i = 0; i < dirs.size(); i++) {
            String dirPath = dirs.get(i);
            if(i != 0 && path.startsWith(dirPath))
                return dirPath;
        }
        return null;
    }

    public static List<File> listFilesRecursive(File directory) {
        List<File> fileList = new ArrayList<>();
        Stack<File> stack = new Stack<File>();
        stack.push(directory);
        while(!stack.isEmpty()) {
            File child = stack.pop();
            if (child.isDirectory()) {
                for(File file : child.listFiles())
                    stack.push(file);
            } else if (child.isFile()) {
                fileList.add(child);
            }
        }
        return fileList;
    }
}
