package org.cryse.unifystorage.explorer.service.operation;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;

public class OperationObserverManager implements OnRemoteOperationListener {
    private static OperationObserverManager instance;
    private Handler mHandler;
    private final ArrayList<OnRemoteOperationListener> mOperationListeners;

    public static void init(Context context) {
        if (instance == null) {
            synchronized (OperationObserverManager.class) {
                if (instance == null) {
                    instance = new OperationObserverManager(context);
                }
            }
        }
    }

    public static OperationObserverManager instance() {
        return instance;
    }

    protected OperationObserverManager(Context context) {
        mHandler = new Handler(context.getMainLooper());
        mOperationListeners = new ArrayList<>();
    }

    public void addOperationListener(OnRemoteOperationListener listener) {
        synchronized (mOperationListeners) {
            mOperationListeners.add(listener);
        }
    }

    public void removeOperationListener(OnRemoteOperationListener listener) {
        synchronized (mOperationListeners) {
            mOperationListeners.remove(listener);
        }
    }

    public void clearAllListener() {
        synchronized (mOperationListeners) {
            mOperationListeners.clear();
        }
    }

    @Override
    public void onRemoteOperationStart(RemoteOperation caller) {
        synchronized (mOperationListeners) {
            for (OnRemoteOperationListener listener : mOperationListeners) {
                if (listener != null) {
                    listener.onRemoteOperationStart(caller);
                }
            }
        }
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result) {
        synchronized (mOperationListeners) {
            for (OnRemoteOperationListener listener : mOperationListeners) {
                if (listener != null) {
                    listener.onRemoteOperationFinish(caller, result);
                }
            }
        }
    }

    @Override
    public void onRemoteOperationProgress(RemoteOperation caller, long current, long total) {
        synchronized (mOperationListeners) {
            for (OnRemoteOperationListener listener : mOperationListeners) {
                if (listener != null) {
                    listener.onRemoteOperationProgress(caller, current, total);
                }
            }
        }
    }
}
