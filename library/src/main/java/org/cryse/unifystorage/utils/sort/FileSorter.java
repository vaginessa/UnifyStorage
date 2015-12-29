package org.cryse.unifystorage.utils.sort;

import org.cryse.unifystorage.AbstractFile;

import java.util.Comparator;

public class FileSorter {
    public enum FileNameComparator implements Comparator<AbstractFile> {
        AESC,DESC;

        public static FileNameComparator getInstance(boolean aesc) {
            return aesc ? AESC : DESC;
        }

        @Override
        public int compare(AbstractFile lhs, AbstractFile rhs) {
            if(this == AESC)
                return lhs.getName().compareTo(rhs.getName());
            else
                return rhs.getName().compareTo(lhs.getName());
        }
    }

    public static int longCompare(long lhs, long rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    public enum FileSizeComparator implements Comparator<AbstractFile> {
        AESC,DESC;

        public static FileSizeComparator getInstance(boolean aesc) {
            return aesc ? AESC : DESC;
        }

        @Override
        public int compare(AbstractFile lhs, AbstractFile rhs) {
            if(this == AESC)
                return longCompare(lhs.size(), rhs.size());
            else
                return longCompare(rhs.size(), lhs.size());
        }
    }

    public enum FileLastModifiedTimeComparator implements Comparator<AbstractFile> {
        AESC,DESC;

        public static FileLastModifiedTimeComparator getInstance(boolean aesc) {
            return aesc ? AESC : DESC;
        }

        @Override
        public int compare(AbstractFile lhs, AbstractFile rhs) {
            if(this == AESC)
                return longCompare(lhs.getLastModifiedTime(), rhs.getLastModifiedTime());
            else
                return longCompare(rhs.getLastModifiedTime(), lhs.getLastModifiedTime());
        }
    }
}
