package org.cryse.unifystorage.explorer.service;

import org.cryse.unifystorage.explorer.event.AbstractEvent;
import org.cryse.unifystorage.explorer.event.EventConst;

public class StopDownloadEvent extends AbstractEvent {
    private int downloadToken;
    public StopDownloadEvent(int downloadToken) {
        this.downloadToken = downloadToken;
    }

    @Override
    public int eventId() {
        return EventConst.EVENT_ID_TASK_DOWNLOAD_FILE_STOP;
    }

    public int getDownloadToken() {
        return downloadToken;
    }
}
