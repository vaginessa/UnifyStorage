package org.cryse.unifystorage.explorer.service.task;

import android.content.Context;
import android.os.Handler;

import org.cryse.unifystorage.explorer.service.operation.OnOperationListener;
import org.cryse.unifystorage.explorer.service.operation.Operation;

public abstract class Task {
    private boolean mShouldQueue;

    public Task(boolean shouldQueue) {
        this.mShouldQueue = shouldQueue;
    }

    public Operation getOperation(Context context) {
        return getOperation(context, null, null);
    }

    public abstract Operation getOperation(Context context, OnOperationListener listener, Handler listenerHandler);

    public boolean shouldQueue() {
        return mShouldQueue;
    }

    public abstract String generateToken();
}
