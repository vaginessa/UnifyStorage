package org.cryse.unifystorage.explorer.service.task;

import android.content.Context;

import org.cryse.unifystorage.explorer.service.operation.Operation;

public abstract class Task {
    private boolean mShouldQueue;

    public Task(boolean shouldQueue) {
        this.mShouldQueue = shouldQueue;
    }

    public abstract Operation getOperation(Context context);
    public abstract Operation.OperationContext getOperationContext(Context context);

    public boolean shouldQueue() {
        return mShouldQueue;
    }

    protected abstract String generateToken();
}
