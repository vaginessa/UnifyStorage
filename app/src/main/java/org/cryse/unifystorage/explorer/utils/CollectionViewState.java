package org.cryse.unifystorage.explorer.utils;

public class CollectionViewState {
    public static final CollectionViewState EMPTY = new CollectionViewState(0, 0f);
    public final int position; // First visible item position
    public final float offset; // First visible item top offset

    public CollectionViewState(int position, float offset) {
        this.position = position;
        this.offset = offset;
    }
}
