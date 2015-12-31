package org.cryse.unifystorage.explorer.utils;

public class CollectionViewState {
    public int position; // First visible item position
    public float offset; // First visible item top offset

    public CollectionViewState() {

    }

    public CollectionViewState(int position, float offset) {
        this.position = position;
        this.offset = offset;
    }
}
