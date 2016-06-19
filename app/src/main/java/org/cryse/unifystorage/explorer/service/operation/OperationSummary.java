package org.cryse.unifystorage.explorer.service.operation;

import android.content.Context;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class OperationSummary {
    public String token;
    public int tokenInt;
    public AtomicReference<OperationState> mState;
    public AtomicLong itemIndex;
    public AtomicLong itemCount;
    public AtomicLong totalReadSize;
    public AtomicLong totalSize;
    public AtomicLong currentItemSize;
    public AtomicLong currentItemReadSize;
    public double totalCountPercent;
    public double totalSizePercent;
    public double currentSizePercent;
    public double displayPercent;

    public AtomicReference<String> title;
    public AtomicReference<String> simpleContent;
    public AtomicReference<String> content;

    public OperationSummary(String token, int tokenInt) {
        this.token = token;
        this.tokenInt = tokenInt;
        mState = new AtomicReference<>(OperationState.UNKNOWN);
        itemCount = new AtomicLong(0);
        itemIndex = new AtomicLong(0);
        totalReadSize = new AtomicLong(0);
        totalSize = new AtomicLong(0);
        currentItemSize = new AtomicLong(0);
        currentItemReadSize = new AtomicLong(0);
        title = new AtomicReference<>("");
        simpleContent = new AtomicReference<>("");
        content = new AtomicReference<>("");
    }

    public void setProgress(long currentItemReadSize,
                            long currentItemSize,
                            long itemIndex,
                            long itemCount,
                            long totalReadSize,
                            long totalSize) {
        this.currentItemReadSize.set(currentItemReadSize);
        this.currentItemSize.set(currentItemSize);
        this.itemIndex.set(itemIndex);
        this.itemCount.set(itemCount);
        this.totalReadSize.set(totalReadSize);
        this.totalSize.set(totalSize);
        this.totalCountPercent = (double) itemIndex/ (double) itemCount ;
        this.currentSizePercent = (double) currentItemReadSize / (double) currentItemSize;
        this.totalSizePercent = (double) totalReadSize / (double) totalSize;
    }
}
