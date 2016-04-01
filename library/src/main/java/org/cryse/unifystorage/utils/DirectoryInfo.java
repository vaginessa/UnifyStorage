package org.cryse.unifystorage.utils;

import org.cryse.unifystorage.RemoteFile;

import java.util.List;

public class DirectoryInfo {
    public final RemoteFile directory;
    public final List<RemoteFile> files;
    public String cursor;
    public boolean hasMore;

    public DirectoryInfo(RemoteFile directory, List<RemoteFile> files) {
        this.directory = directory;
        this.files = files;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DirectoryInfo)) {
            return false;
        }
        DirectoryInfo p = (DirectoryInfo) o;
        return objectsEqual(p.directory, directory) && objectsEqual(p.files, files);
    }

    private static boolean objectsEqual(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    @Override
    public int hashCode() {
        return (directory == null ? 0 : directory.hashCode()) ^ (files == null ? 0 : files.hashCode());
    }

    public static DirectoryInfo create(RemoteFile a, List<RemoteFile> b) {
        return new DirectoryInfo(a, b);
    }
}
