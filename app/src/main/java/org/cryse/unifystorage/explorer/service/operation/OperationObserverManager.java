package org.cryse.unifystorage.explorer.service.operation;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;

public class OperationObserverManager implements OnOperationListener {
    private static OperationObserverManager instance;
    private Handler mHandler;
    private final ArrayList<OnOperationListener> mOperationListeners;

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

    public void addOperationListener(OnOperationListener listener) {
        synchronized (mOperationListeners) {
            mOperationListeners.add(listener);
        }
    }

    public void removeOperationListener(OnOperationListener listener) {
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
    public void onOperationStateChanged(Operation operation, OperationState state) {
        synchronized (mOperationListeners) {
            for (OnOperationListener listener : mOperationListeners) {
                if (listener != null) {
                    listener.onOperationStateChanged(operation, state);
                }
            }
        }
    }

    @Override
    public void onOperationProgress(Operation operation, long currentRead, long currentSize, long itemIndex, long itemCount, long totalRead, long totalSize) {
        synchronized (mOperationListeners) {
            for (OnOperationListener listener : mOperationListeners) {
                if (listener != null) {
                    listener.onOperationProgress(operation, currentRead, currentSize, itemIndex, itemCount, totalRead, totalSize);
                }
            }
        }
    }
}
