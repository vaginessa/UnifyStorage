package org.cryse.unifystorage.explorer.utils;

import android.webkit.MimeTypeMap;

public class MimeUtils {
    public static String getMime(String path) {
        MimeTypeMap mimeMap = MimeTypeMap.getSingleton();
        String extension = MimeTypeMap.getFileExtensionFromUrl(path.replace(" ",""));
        return mimeMap.getMimeTypeFromExtension(extension);
    }

    public static boolean isApkMime(String mime) {
        return "application/vnd.android.package-archive".equals(mime);
    }
}
