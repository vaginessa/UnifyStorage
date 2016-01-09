package org.cryse.unifystorage.explorer.event;

public class FileDeleteEvent extends AbstractEvent {
    public int providerId;
    public String targetId;
    public long currentCount;
    public long totalFileCount;
    public String fileId;
    public String fileName;
    public boolean success;
    public FileDeleteEvent(int providerId, String targetId, long currentCount, long totalFileCount, String fileId, String fileName, boolean success) {
        this.providerId = providerId;
        this.targetId = targetId;
        this.currentCount = currentCount;
        this.totalFileCount = totalFileCount;
        this.fileId = fileId;
        this.fileName = fileName;
        this.success = success;
    }

    @Override
    public int eventId() {
        return EventConst.EVENT_ID_FILE_DELETE;
    }
}
