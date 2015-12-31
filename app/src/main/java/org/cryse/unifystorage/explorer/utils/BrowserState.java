package org.cryse.unifystorage.explorer.utils;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.utils.DirectoryPair;

import java.util.List;

public class BrowserState<RF extends RemoteFile> {
    public DirectoryPair<RF, List<RF>> directory;
    public CollectionViewState collectionViewState;
    public float scrollOffset;

    public BrowserState() {

    }

    public BrowserState(DirectoryPair<RF, List<RF>> directory, CollectionViewState collectionViewState) {
        this.directory = directory;
        this.collectionViewState = collectionViewState;
    }
}
