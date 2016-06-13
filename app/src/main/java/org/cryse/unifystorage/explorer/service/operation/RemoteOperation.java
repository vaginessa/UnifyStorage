package org.cryse.unifystorage.explorer.service.operation;

import android.content.Context;
import android.os.Handler;

import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.explorer.application.StorageProviderManager;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;

public abstract class RemoteOperation extends Operation<RemoteOperation.RemoteOperationContext, RemoteOperationResult> {
    private static final String TAG = RemoteOperation.class.getSimpleName();

    private RemoteOperationContext mOperationContext;

    private OnRemoteOperationListener mListener = null;
    private Handler mListenerHandler = null;

    public RemoteOperation(String operationToken) {
        super(operationToken);
    }

    @Override
    protected abstract RemoteOperationResult run(RemoteOperationContext operationContext, OnRemoteOperationListener listener, Handler listenerHandler);

    public boolean shouldRefresh() {
        return true;
    }

    @Override
    public RemoteOperationResult execute(RemoteOperationContext operationContext,
                                   OnRemoteOperationListener listener, Handler listenerHandler) {
        mOperationContext = operationContext;
        return run(mOperationContext, listener, listenerHandler);
    }

    public Thread executeAsync(RemoteOperationContext context,
                               OnRemoteOperationListener listener, Handler listenerHandler) {
        mOperationContext = context;
        mListener = listener;
        mListenerHandler = listenerHandler;

        Thread runnerThread = new Thread(this);
        runnerThread.start();
        return runnerThread;
    }

    @Override
    public final void run() {
        RemoteOperationResult result = null;
        if(mOperationContext == null || mOperationContext.getStorageProvider() == null) {
                // Log_OC.e(TAG, "Error while trying to access to " + mAccount.name, e);
                result = new RemoteOperationResult(new IllegalArgumentException());
        }

        // First Check status here

        // If there's no error, run
        if (result == null)
            result = run(mOperationContext, mListener, mListenerHandler);

        final RemoteOperationResult resultToSend = result;
        if (mListenerHandler != null && mListener != null) {
            mListenerHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onRemoteOperationFinish(RemoteOperation.this, resultToSend);
                }
            });
        }
    }

    public final RemoteOperationContext getOperationContext() {
        return mOperationContext;
    }

    public static class RemoteOperationContext extends Operation.OperationContext {
        private StorageProvider mStorageProvider;
        private StorageProviderInfo mStorageProviderInfo;

        public RemoteOperationContext(Context context, StorageProviderInfo providerInfo) {
            mStorageProviderInfo = providerInfo;
            mStorageProvider = StorageProviderManager.instance().createStorageProvider(
                    context,
                    providerInfo
            );
        }

        public StorageProvider getStorageProvider() {
            return mStorageProvider;
        }

        public int getStorageProviderId() {
            return mStorageProviderInfo.getStorageProviderId();
        }
    }
}