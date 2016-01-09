package org.cryse.unifystorage.explorer.event;

public class FileDeleteResultEvent extends AbstractEvent {
    public int providerId;
    public String targetId;
    public boolean succes;
    public String errorFileName;
    public String errorMessage;
    public FileDeleteResultEvent(int providerId, String targetId, boolean succes, String errorFileName, String errorMessage) {
        this.providerId = providerId;
        this.targetId = targetId;
        this.succes = succes;
        this.errorFileName = errorFileName;
        this.errorMessage = errorMessage;
    }

    @Override
    public int eventId() {
        return EventConst.EVENT_ID_FILE_DELETE_RESULT;
    }
}
