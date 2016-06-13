package org.cryse.unifystorage.explorer.service.operation;

public interface OnRemoteOperationListener {
    void onRemoteOperationStart(Operation operation);
    void onRemoteOperationFinish(Operation operation, RemoteOperationResult result);
    void onRemoteOperationProgress(Operation operation, long current, long total);
}