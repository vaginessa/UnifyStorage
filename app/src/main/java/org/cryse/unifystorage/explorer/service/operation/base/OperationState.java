package org.cryse.unifystorage.explorer.service.operation.base;

public enum OperationState {
    UNKNOWN,
    NEW,
    PREPARING,
    RUNNING,
    PAUSE,
    BLOCKED,
    VERIFYING,
    COMPLETED,
    FAILED
}
