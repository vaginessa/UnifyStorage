package org.cryse.unifystorage.explorer.service.operation;

import android.os.Handler;

public abstract class Operation<T extends Operation.OperationContext , R extends Operation.OperationResult> implements Runnable {
    private String mOperationToken;

    public Operation(String operationToken) {
        this.mOperationToken = operationToken;
    }

    public abstract String getOperationName();

    public String getOperationToken() {
        return mOperationToken;
    }

    protected abstract R run(T operationContext, OnRemoteOperationListener listener, Handler listenerHandler);
    public abstract void run();

    public R execute(T operationContext) {
        return execute(operationContext, null, null);
    }

    public abstract R execute(T operationContext, OnRemoteOperationListener listener, Handler listenerHandler);

    public Thread executeAsync(T operationContext) {
        return executeAsync(operationContext, null, null);
    }

    public abstract Thread executeAsync(T operationContext, OnRemoteOperationListener listener, Handler listenerHandler);

    public abstract T getOperationContext();

    public static class OperationContext {

    }

    public static class OperationResult {

    }
}
