package org.cryse.unifystorage.explorer.service.operation.base;


import android.content.Context;

import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.utils.FileSizeUtils;

public class OperationSummary {
    public String token;
    public int tokenInt;
    public OperationState state;
    public long itemIndex;
    public long itemCount;
    public long totalReadSize;
    public long totalSize;
    public long currentItemSize;
    public long currentItemReadSize;
    public long currentSpeed;
    public double totalCountPercent;
    public double totalSizePercent;
    public double currentSizePercent;

    public OperationSummary(String token, int tokenInt) {
        this.token = token;
        this.tokenInt = tokenInt;
        state = OperationState.UNKNOWN;
        itemCount = 0;
        itemIndex = 0;
        totalReadSize = 0;
        totalSize = 0;
        currentItemSize = 0;
        currentItemReadSize = 0;
        currentSpeed = 0;
    }

    public void setProgress(long currentItemReadSize,
                            long currentItemSize,
                            long currentSpeed,
                            long itemIndex,
                            long itemCount,
                            long totalReadSize,
                            long totalSize) {
        this.currentItemReadSize = currentItemReadSize;
        this.currentItemSize = currentItemSize;
        this.currentSpeed = currentSpeed;
        this.itemIndex = itemIndex;
        this.itemCount = itemCount;
        this.totalReadSize = totalReadSize;
        this.totalSize = totalSize;
        this.totalCountPercent = (double) itemIndex/ (double) itemCount ;
        this.currentSizePercent = (double) currentItemReadSize / (double) currentItemSize;
        this.totalSizePercent = (double) totalReadSize / (double) totalSize;
    }

    public String currentProgressDesc(Context context) {
        return context.getString(
                R.string.operation_progress_current_item,
                FileSizeUtils.humanReadableByteCount(currentItemReadSize, false),
                FileSizeUtils.humanReadableByteCount(currentItemSize, false)
        );
    }

    public String totalCountProgressDesc(Context context) {
        return context.getString(
                R.string.operation_progress_total_count,
                itemIndex,
                itemCount
        );
    }

    public String totalSizeProgressDesc(Context context) {
        return context.getString(
                R.string.operation_progress_total_size,
                FileSizeUtils.humanReadableByteCount(totalReadSize, false),
                FileSizeUtils.humanReadableByteCount(totalSize, false)
        );
    }
}
