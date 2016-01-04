package org.cryse.unifystorage.explorer.utils;

import android.content.Context;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.utils.Path;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LocalCachesUtils {
    public static <RF extends RemoteFile> String getFullCachePath(
            Context context,
            String providerName,
            String providerUuid,
            RF file
    ) {
        String cacheFileDirectory = getCacheFileDirectory(providerName, providerUuid, file);
        String appFilesDirectory = context.getFilesDir().getPath();
        return Path.combine(Path.combine(appFilesDirectory, cacheFileDirectory),file.getName());
    }

    public static <RF extends RemoteFile>  String getCacheFileDirectory(
            String providerName,
            String providerUuid,
            RF file
    ) {
        String hashOriginal = file.getPath() + "::" + file.getName() + "::" + Long.toString(file.size());
        String hash = md5hash(hashOriginal);
        return "providers" + "/" + providerName + "/" + providerUuid + "/" + hash + "/";
    }

    public static String md5hash(String in) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(in.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
}
