package org.cryse.unifystorage.explorer.service.operation.base;

public interface OnOperationListener {
    void onOperationStateChanged(Operation operation, OperationState state);
    void onOperationProgress(
            Operation operation,
            long currentItemRead,
            long currentItemSize,
            long currentSpeed,
            long itemIndex,
            long itemCount,
            long totalRead,
            long totalSize
    );
}
