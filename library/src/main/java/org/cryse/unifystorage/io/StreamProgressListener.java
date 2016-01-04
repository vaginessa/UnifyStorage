package org.cryse.unifystorage.io;

public interface StreamProgressListener {
    void onProgress(ProgressInputStream stream, long current, long total, double rate);
}
