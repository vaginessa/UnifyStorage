package org.cryse.unifystorage.explorer.service.operation;

import android.support.v4.app.NotificationCompat;

import java.util.concurrent.atomic.AtomicInteger;

public class OperationStatus {
    private static AtomicInteger mTokenIntCounter = new AtomicInteger(20000);
    private String mToken;
    private int mTokenInt;
    private Thread mWorkerThread;
    private Operation mOperation;
    private NotificationCompat.Builder mNotificationBuilder;
    private boolean mShouldShowNotification;
    private long mCurrent;
    private long mTotal;

    public OperationStatus(String token, Operation operation, Thread workerThread, boolean shouldShowNotification) {
        this.mToken = token;
        this.mTokenInt = mTokenIntCounter.incrementAndGet();
        this.mWorkerThread = workerThread;
        this.mOperation = operation;
        this.mShouldShowNotification = shouldShowNotification;
    }

    public void cancel() {
        if(mWorkerThread != null && !mWorkerThread.isInterrupted() && mWorkerThread.isAlive()) {
            mWorkerThread.interrupt();
        }
    }

    public void setProgress(long current, long total) {
        mCurrent = current;
        mTotal = total;
    }

    public long getCurrent() {
        return mCurrent;
    }

    public long getTotal() {
        return mTotal;
    }

    public void setShouldShowNotification(boolean shouldShowNotification) {
        this.mShouldShowNotification = shouldShowNotification;
    }

    public boolean shouldShowNotification() {
        return mShouldShowNotification;
    }

    public String getToken() {
        return mToken;
    }

    public int getTokenInt() {
        return mTokenInt;
    }

    public Operation getOperation() {
        return mOperation;
    }

    public NotificationCompat.Builder getNotificationBuilder() {
        return mNotificationBuilder;
    }

    public void setNotificationBuilder(NotificationCompat.Builder notificationBuilder) {
        this.mNotificationBuilder = notificationBuilder;
    }

    public static class Builder {
        private String mToken;
        private Thread mWorkerThread = null;
        private Operation mOperation = null;
        private boolean mShowNotification = false;
        public Builder() {

        }

        public Builder token(String token) {
            mToken = token;
            return this;
        }

        public Builder workThread(Thread thread) {
            mWorkerThread = thread;
            return this;
        }

        public Builder operation(Operation operation) {
            mOperation = operation;
            return this;
        }

        public Builder showNotification(boolean showNotification) {
            mShowNotification = showNotification;
            return this;
        }

        public OperationStatus build() {
            return new OperationStatus(mToken, mOperation, mWorkerThread, mShowNotification);
        }
    }
}
