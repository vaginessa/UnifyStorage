package org.cryse.unifystorage.explorer.utils;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.utils.DirectoryInfo;

import java.util.List;

public class BrowserState {
    public DirectoryInfo directory;
    public CollectionViewState collectionViewState;

    public BrowserState() {

    }

    public BrowserState(DirectoryInfo directory, CollectionViewState collectionViewState) {
        this.directory = directory;
        this.collectionViewState = collectionViewState;
    }

    public BrowserState(DirectoryInfo directory, CollectionViewState collectionViewState, List<RemoteFile> hiddenFiles) {
        this.directory = directory;
        this.collectionViewState = collectionViewState;
        this.directory.files.addAll(hiddenFiles);
    }
}
