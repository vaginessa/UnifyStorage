package org.cryse.unifystorage.explorer.service.operation.base;

import android.content.Context;
import android.os.Handler;

import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.explorer.application.StorageProviderManager;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;


public abstract class RemoteOperation<P extends RemoteOperation.Params> extends Operation<P, RemoteOperationResult> {
    private static final String TAG = RemoteOperation.class.getSimpleName();

    public RemoteOperation(String token, P params) {
        super(token, params);
    }

    public RemoteOperation(String token, P params, OnOperationListener listener, Handler listenerHandler) {
        super(token, params, listener, listenerHandler);
    }

    public boolean shouldRefresh() {
        return true;
    }

    public static class Params extends Operation.Params {
        private StorageProvider mSourceStorageProvider;
        private StorageProvider mTargetStorageProvider;
        private StorageProviderInfo mSourceStorageProviderInfo;
        private StorageProviderInfo mTargetStorageProviderInfo;

        public Params(Context context, StorageProviderInfo sourceProviderInfo, StorageProviderInfo targetProviderInfo) {
            this.mSourceStorageProviderInfo = sourceProviderInfo;
            this.mTargetStorageProviderInfo = targetProviderInfo;

            mSourceStorageProvider = StorageProviderManager.instance().createStorageProvider(
                    context,
                    mSourceStorageProviderInfo
            );
            if(targetProviderInfo.getStorageProviderId() == sourceProviderInfo.getStorageProviderId() &&
                    targetProviderInfo.getStorageProviderType() == sourceProviderInfo.getStorageProviderType()) {
                mTargetStorageProvider = mSourceStorageProvider;
            } else {
                mTargetStorageProvider = StorageProviderManager.instance().createStorageProvider(
                        context,
                        mTargetStorageProviderInfo
                );
            }
        }

        public StorageProvider getSourceStorageProvider() {
            return mSourceStorageProvider;
        }

        public StorageProvider getTargetStorageProvider() {
            return mTargetStorageProvider;
        }

        public int getSourceStorageProviderId() {
            return mSourceStorageProviderInfo.getStorageProviderId();
        }

        public int getTargetStorageProviderId() {
            return mTargetStorageProviderInfo.getStorageProviderId();
        }
    }
}