package org.cryse.unifystorage.explorer.event;

import org.cryse.unifystorage.explorer.service.operation.base.Operation;

public class ShowProgressEvent extends AbstractEvent {
    private Operation mOperation;

    public ShowProgressEvent(Operation operation) {
        this.mOperation = operation;
    }

    public Operation getOperation() {
        return mOperation;
    }

    @Override
    public int eventId() {
        return EventConst.EVENT_ID_SHOW_PROGRESS;
    }
}
