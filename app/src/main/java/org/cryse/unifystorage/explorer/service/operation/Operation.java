package org.cryse.unifystorage.explorer.service.operation;

import android.os.Handler;

import org.cryse.unifystorage.explorer.executor.ServiceExecutors;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Operation<P extends Operation.Params , R extends Operation.Result> implements Callable<R> {
    private static AtomicInteger mTokenIntCounter = new AtomicInteger(20000);
    private String mToken;
    private int mTokenInt;
    private P mParams;
    private OnOperationListener mListener;
    private Handler mListenerHandler;
    private OperationSummary mSummary;
    private R mResult;
    private AtomicBoolean mCancel = new AtomicBoolean(false);
    private final Object mPauseLock = new Object();

    public Operation(String token, P params) {
        this(token, params, null, null);
    }

    public Operation(String token, P params, OnOperationListener listener, Handler listenerHandler) {
        this.mToken = token;
        this.mTokenInt = mTokenIntCounter.incrementAndGet();
        this.mSummary = new OperationSummary(mToken, mTokenInt);
        this.mParams = params;
        this.mListener = listener;
        this.mListenerHandler = listenerHandler;
        notifyOperationState(OperationState.NEW);
    }

    @Override
    public R call() {
        synchronized (mPauseLock) {
            R result = null;
            notifyOperationState(OperationState.PREPARING);
            prepareOperation();
            notifyOperationState(OperationState.RUNNING);
            result = runOperation();
            notifyOperationState(OperationState.VERIFYING);
            verifyOperation(result);
            mResult = result;
            if(mResult.isSuccess())
                onOperationCompleted();
            else
                onOperationFailed();
            return result;
        }
    }

    public R execute() {
        return call();
    }

    public void execute(Executor executor) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                call();
            }
        };
        executor.execute(runnable);
    }

    public void executeInBackground() {
        execute(ServiceExecutors.background());
    }

    public void cancel() {
        mCancel.set(true);
    }

    protected boolean shouldCancel() {
        return mCancel.get();
    }

    protected void prepareOperation() {

    }

    protected abstract R runOperation();

    protected void verifyOperation(R resultToVerify) {

    }

    protected void onOperationCompleted() {
        notifyOperationState(OperationState.COMPLETED);
    }

    protected void onOperationPaused() {
        notifyOperationState(OperationState.PAUSE);
    }

    protected void onOperationBlocked() {
        notifyOperationState(OperationState.BLOCKED);
    }

    protected void onOperationFailed() {
        notifyOperationState(OperationState.FAILED);
    }

    protected abstract void onBuildNotificationForState(OperationState state);

    protected abstract void onBuildNotificationForProgress(
            final long currentRead,
            final long currentSize,
            final long itemIndex,
            final long itemCount,
            final long totalRead,
            final long totalSize
    );

    protected void notifyOperationState(final OperationState state) {
        setState(state);
        onBuildNotificationForState(state);
        if (getListenerHandler() != null && getListener() != null) {
            getListenerHandler().post(new Runnable() {
                @Override
                public void run() {
                    getListener().onOperationStateChanged(
                            Operation.this,
                            state
                    );
                }
            });
        }
    }

    protected void notifyOperationProgress(
            final long currentRead,
            final long currentSize,
            final long itemIndex,
            final long itemCount,
            final long totalRead,
            final long totalSize
    ) {
        mSummary.setProgress(currentRead, currentSize, itemIndex, itemCount, totalRead, totalSize);
        onBuildNotificationForProgress(currentRead, currentSize, itemIndex, itemCount, totalRead, totalSize);
        if (getListenerHandler() != null && getListener() != null) {
            getListenerHandler().post(new Runnable() {
                @Override
                public void run() {
                    getListener().onOperationProgress(
                            Operation.this,
                            currentRead,
                            currentSize,
                            itemIndex,
                            itemCount,
                            totalRead,
                            totalSize
                    );
                }
            });
        }
    }

    public abstract String getOperationName();

    public String getToken() {
        return mToken;
    }

    public int getTokenInt() {
        return mTokenInt;
    }

    protected P getParams() {
        return mParams;
    }

    public R getResult() {
        return mResult;
    }

    public OperationState getState() {
        return mSummary.mState.get();
    }

    public void setState(OperationState state) {
        this.mSummary.mState.set(state);
    }

    public void setListener(OnOperationListener listener, Handler listenerHandler) {
        this.mListener = listener;
        this.mListenerHandler = listenerHandler;
    }

    public OnOperationListener getListener() {
        return mListener;
    }

    public Handler getListenerHandler() {
        return mListenerHandler;
    }

    public OperationSummary getSummary() {
        return mSummary;
    }

    public void pause() throws InterruptedException {
        synchronized (mPauseLock) {
            mPauseLock.wait();
        }
    }

    public void resume() {
        synchronized (mPauseLock) {
            mPauseLock.notify();
        }
    }

    public static class Params {

    }

    public static abstract class Result {
        public abstract boolean isSuccess();
        public abstract Exception getException();
    }
}
