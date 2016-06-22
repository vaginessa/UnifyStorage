package org.cryse.unifystorage.explorer.files;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.widget.Selectable;

import java.util.ArrayList;
import java.util.List;

public class RemoteFileWrapper implements Selectable {
    private RemoteFile mFile;
    private boolean mSelected;

    public static List<RemoteFileWrapper> wrap(List<RemoteFile> fileList) {
        List<RemoteFileWrapper> wrappers = new ArrayList<>(fileList.size());
        for(RemoteFile file : fileList) {
            wrappers.add(new RemoteFileWrapper(file));
        }
        return wrappers;
    }

    public static List<RemoteFile> toList(List<RemoteFileWrapper> fileList) {
        List<RemoteFile> files = new ArrayList<>(fileList.size());
        for(RemoteFileWrapper fileWrapper : fileList) {
            files.add(fileWrapper.getRemoteFile());
        }
        return files;
    }

    public static RemoteFile[] toArray(List<RemoteFileWrapper> fileList) {
        RemoteFile[] files = new RemoteFile[fileList.size()];
        for (int i = 0; i < fileList.size(); i++) {
            RemoteFileWrapper fileWrapper = fileList.get(i);
            files[i] = fileWrapper.getRemoteFile();
        }
        return files;
    }

    public static RemoteFile[] toArray(RemoteFileWrapper[] fileList) {
        RemoteFile[] files = new RemoteFile[fileList.length];
        for (int i = 0; i < fileList.length; i++) {
            RemoteFileWrapper fileWrapper = fileList[i];
            files[i] = fileWrapper.getRemoteFile();
        }
        return files;
    }

    public RemoteFileWrapper(RemoteFile file) {
        this.mFile = file;
    }

    public RemoteFile getRemoteFile() {
        return mFile;
    }

    @Override
    public boolean isSelected() {
        return mSelected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.mSelected = selected;
    }
}
