package org.cryse.unifystorage.utils;

import org.cryse.unifystorage.AbstractFile;
import org.cryse.unifystorage.RemoteFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DirectoryInfo {
    public final RemoteFile directory;
    public final List<RemoteFile> files;
    public final List<RemoteFile> hiddenFiles;

    public String cursor;
    public boolean hasMore;

    public static DirectoryInfo create(RemoteFile a, List<RemoteFile> b) {
        return new DirectoryInfo(a, b);
    }

    public static DirectoryInfo fromDirectory(RemoteFile directory) {
        return new DirectoryInfo(directory, new ArrayList<RemoteFile>());
    }

    public static DirectoryInfo create(RemoteFile a, List<RemoteFile> b, boolean hasMore, String cursor) {
        DirectoryInfo directoryInfo = new DirectoryInfo(a, b);
        directoryInfo.hasMore = hasMore;
        directoryInfo.cursor = cursor;
        return directoryInfo;
    }

    public DirectoryInfo(RemoteFile directory, List<RemoteFile> files) {
        this.directory = directory;
        this.files = files;
        this.hiddenFiles = new LinkedList<>();
        this.hasMore = false;
        this.cursor = null;
    }

    public void setShowHiddenFiles(boolean show) {
        if (!show) {
            for (Iterator<RemoteFile> iterator = files.iterator(); iterator.hasNext(); ) {
                RemoteFile file = iterator.next();
                if (file.getName().startsWith(".")) {
                    iterator.remove();
                    hiddenFiles.add(file);
                }
            }
        } else {
            if (!hiddenFiles.isEmpty()) {
                files.addAll(hiddenFiles);
                hiddenFiles.clear();
            }
        }
    }

    public void sort(Comparator<AbstractFile> fileComparator) {
        Collections.sort(files, fileComparator);
    }

    public void setShowHiddenFiles(boolean show, Comparator<AbstractFile> fileComparator) {
        setShowHiddenFiles(show);
        sort(fileComparator);
    }

    public void clear() {
        this.hasMore = false;
        this.cursor = null;
        this.files.clear();
        this.hiddenFiles.clear();
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
}
