package org.cryse.unifystorage.explorer.utils;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.utils.DirectoryInfo;

import java.util.List;

public class BrowserState<RF extends RemoteFile> {
    public DirectoryInfo<RF, List<RF>> directory;
    public CollectionViewState collectionViewState;
    public List<RF> hiddenFiles;

    public BrowserState() {

    }

    public BrowserState(DirectoryInfo<RF, List<RF>> directory, CollectionViewState collectionViewState, List<RF> hiddenFiles) {
        this.directory = directory;
        this.collectionViewState = collectionViewState;
        this.hiddenFiles = hiddenFiles;
    }
}
