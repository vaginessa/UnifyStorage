package org.cryse.unifystorage.utils;

import android.util.Log;

import org.cryse.unifystorage.RemoteFile;

import java.net.URI;

public final class Path {

    public static final String ROOT = "/";
    public static final String SEPARATOR = "/";
    public static final String EMPTY = "";

    public static String combine(RemoteFile remoteFile, String title){
        if(remoteFile != null)
            return combine(remoteFile.getPath(), title);
        else
            return combine(EMPTY, title);
    }

    public static String combine(String path1, String path2){
        String p1 = emptyIfNull(path1);
        String p2 = emptyIfNull(path2);

        if(p1.endsWith(SEPARATOR) || p2.startsWith(SEPARATOR))
            return p1 + p2;
        else
            return p1 + SEPARATOR + p2;
    }

    /**
     * Returns the directory of this file
     */
    public static String getDirectory(String path){
        if(path == null) throw new NullPointerException("path can't be null");

        path = path.trim();
        if(path.equals(ROOT)) return null;

        if(path.endsWith(SEPARATOR))
            path = path.substring(0, path.length() - 1);

        int lastIndex = path.lastIndexOf(SEPARATOR);
        if(lastIndex > 0){
            return path.substring(0, lastIndex);
        }

        return SEPARATOR;
    }

    public static String getFileName(String path){
        if(path == null) return null;

        path = path.trim();
        if(path.equals(ROOT)) return ROOT;

        if(path.endsWith(SEPARATOR))
            path = path.substring(0, path.length() - 1);

        int lastIndex = path.lastIndexOf(SEPARATOR);
        if(lastIndex > -1){
            return path.substring(lastIndex + 1);
        }

        return path;
    }

    public static boolean isEqualOrDirectChild(String path1, String path2) {
        if(path2.startsWith(path1)) {
            String relative = path2.replace(path1, "");
            return relative.indexOf('/') == relative.lastIndexOf("/");
        } else {
            return false;
        }
    }

    private static String emptyIfNull(String str){
        if(str == null) return EMPTY;
        return str;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    private Path() {
        // nothing
    }
}
