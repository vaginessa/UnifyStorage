package org.cryse.unifystorage.utils;

import org.cryse.unifystorage.RemoteFile;

import java.util.List;

public class DirectoryPair<RF extends RemoteFile, FL extends List<RF>>{
    public final RF directory;
    public final FL files;

    public DirectoryPair(RF first, FL second) {
        this.directory = first;
        this.files = second;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DirectoryPair)) {
            return false;
        }
        DirectoryPair<?, ?> p = (DirectoryPair<?, ?>) o;
        return objectsEqual(p.directory, directory) && objectsEqual(p.files, files);
    }

    private static boolean objectsEqual(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    @Override
    public int hashCode() {
        return (directory == null ? 0 : directory.hashCode()) ^ (files == null ? 0 : files.hashCode());
    }

    public static <RF extends RemoteFile, FL extends List<RF>> DirectoryPair <RF, FL> create(RF a, FL b) {
        return new DirectoryPair<RF, FL>(a, b);
    }
}
