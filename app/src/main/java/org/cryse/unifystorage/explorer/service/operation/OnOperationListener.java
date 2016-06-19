package org.cryse.unifystorage.explorer.service.operation;

public interface OnOperationListener {
    void onOperationStateChanged(Operation operation, OperationState state);
    void onOperationProgress(
            Operation operation,
            long currentItemRead,
            long currentItemSize,
            long itemIndex,
            long itemCount,
            long totalRead,
            long totalSize
    );
}
