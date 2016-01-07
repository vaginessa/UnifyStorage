package org.cryse.unifystorage.utils;

import org.cryse.unifystorage.RemoteFile;

import java.util.List;

public class DirectoryInfo<RF extends RemoteFile, FL extends List<RF>> {
    public final RF directory;
    public final FL files;

    public DirectoryInfo(RF directory, FL files) {
        this.directory = directory;
        this.files = files;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DirectoryInfo)) {
            return false;
        }
        DirectoryInfo<?, ?> p = (DirectoryInfo<?, ?>) o;
        return objectsEqual(p.directory, directory) && objectsEqual(p.files, files);
    }

    private static boolean objectsEqual(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    @Override
    public int hashCode() {
        return (directory == null ? 0 : directory.hashCode()) ^ (files == null ? 0 : files.hashCode());
    }

    public static <RF extends RemoteFile, FL extends List<RF>> DirectoryInfo<RF, FL> create(RF a, FL b) {
        return new DirectoryInfo<RF, FL>(a, b);
    }
}
