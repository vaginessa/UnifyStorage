package org.cryse.unifystorage.explorer.utils.http;

public interface DownloadListener {
    void onStart(String taskToken);
    void onFinished(String taskToken);
    void onCancelled(String taskToken);
    void onError(String taskToken, Throwable throwable);
    void onProgress(String taskToken, long readSize, long totalSize, float percent);
}