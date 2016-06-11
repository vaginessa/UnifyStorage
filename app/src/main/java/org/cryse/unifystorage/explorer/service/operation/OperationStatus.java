package org.cryse.unifystorage.explorer.service.operation;


public class OperationStatus {
    private String mToken;
    private Thread mWorkerThread;
    private Operation mOperation;
    private boolean mShouldShowNotification;

    public OperationStatus(String token, Operation operation, Thread workerThread, boolean shouldShowNotification) {
        this.mToken = token;
        this.mWorkerThread = workerThread;
        this.mOperation = operation;
    }

    public void cancel() {
        if(mWorkerThread != null && !mWorkerThread.isInterrupted() && mWorkerThread.isAlive()) {
            mWorkerThread.interrupt();
        }
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
