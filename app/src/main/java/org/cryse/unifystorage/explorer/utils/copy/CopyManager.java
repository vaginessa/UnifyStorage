package org.cryse.unifystorage.explorer.utils.copy;

import android.content.Context;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.event.CancelSelectCopyEvent;
import org.cryse.unifystorage.explorer.event.RxEventBus;
import org.cryse.unifystorage.explorer.event.SelectCopyEvent;

public class CopyManager {
    private static CopyManager instance;
    private CopyTask mCurrentCopyTask;

    public static void init(Context context) {
        if (instance == null) {
            synchronized (CopyManager.class) {
                if (instance == null) {
                    instance = new CopyManager();
                }
            }
        }
    }

    public CopyTask getCurrentCopyTask() {
        return mCurrentCopyTask;
    }

    public static CopyManager getInstance() {
        return instance;
    }

    public boolean hasCopyTask() {
        return mCurrentCopyTask != null;
    }

    public <RF extends RemoteFile> void setCopyTask(CopyTask<RF> copyTask) {
        this.mCurrentCopyTask = copyTask;
        RxEventBus.getInstance().sendEvent(new SelectCopyEvent());
    }

    public void cancelCopyTask() {
        this.mCurrentCopyTask = null;
        RxEventBus.getInstance().sendEvent(new CancelSelectCopyEvent());
    }
}
