package org.cryse.unifystorage.explorer.event;

public class CancelSelectCopyEvent extends AbstractEvent {
    @Override
    public int eventId() {
        return EventConst.EVENT_ID_CANCEL_SELECT_COPY_EVENT;
    }
}
