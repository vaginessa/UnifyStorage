package org.cryse.unifystorage.utils;

public interface ProgressCallback {
    void onSuccess();

    void onFailure(Throwable throwable);

    void onProgress(long current, long max);
}
