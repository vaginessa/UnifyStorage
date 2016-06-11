package org.cryse.unifystorage.explorer.service;

public class OperationThread extends Thread {
    public OperationThread(Runnable runnable, String threadName) {
        super(runnable, threadName);

    }


}
