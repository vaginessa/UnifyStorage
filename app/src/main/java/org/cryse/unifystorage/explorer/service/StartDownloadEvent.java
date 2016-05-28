package org.cryse.unifystorage.explorer.service;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.event.AbstractEvent;
import org.cryse.unifystorage.explorer.event.EventConst;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;

public class StartDownloadEvent extends AbstractEvent {
    private StorageProviderInfo storageProviderInfo;
    private int downloadToken;
    private RemoteFile remoteFile;
    private String savePath;
    private boolean openAfterDownload;

    public StartDownloadEvent(StorageProviderInfo storageProviderInfo, int downloadToken, RemoteFile remoteFile, String savePath, boolean openAfterDownload) {
        this.storageProviderInfo = storageProviderInfo;
        this.downloadToken = downloadToken;
        this.remoteFile = remoteFile;
        this.savePath = savePath;
        this.openAfterDownload = openAfterDownload;
    }

    @Override
    public int eventId() {
        return EventConst.EVENT_ID_TASK_DOWNLOAD_FILE_START;
    }

    public StorageProviderInfo getStorageProviderInfo() {
        return storageProviderInfo;
    }

    public int getDownloadToken() {
        return downloadToken;
    }

    public void setDownloadToken(int downloadToken) {
        this.downloadToken = downloadToken;
    }

    public RemoteFile getRemoteFile() {
        return remoteFile;
    }

    public void setRemoteFile(RemoteFile remoteFile) {
        this.remoteFile = remoteFile;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public boolean isOpenAfterDownload() {
        return openAfterDownload;
    }

    public void setOpenAfterDownload(boolean openAfterDownload) {
        this.openAfterDownload = openAfterDownload;
    }
}
