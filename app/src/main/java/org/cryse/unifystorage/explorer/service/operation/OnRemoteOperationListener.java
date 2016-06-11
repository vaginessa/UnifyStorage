package org.cryse.unifystorage.explorer.service.operation;

public interface OnRemoteOperationListener {
    void onRemoteOperationStart(RemoteOperation caller);
    void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result);
    void onRemoteOperationProgress(RemoteOperation caller, long current, long total);
}