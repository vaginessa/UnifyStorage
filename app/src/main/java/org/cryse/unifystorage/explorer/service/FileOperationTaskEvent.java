package org.cryse.unifystorage.explorer.service;

import org.cryse.unifystorage.explorer.event.AbstractEvent;
import org.cryse.unifystorage.explorer.event.EventConst;

public class FileOperationTaskEvent extends AbstractEvent {
    private FileOperation mFileOperation;

    public FileOperationTaskEvent(FileOperation fileOperation) {
        this.mFileOperation = fileOperation;
    }

    public FileOperation getFileOperation() {
        return mFileOperation;
    }

    @Override
    public int eventId() {
        return EventConst.EVENT_ID_TASK_FILE_OPERATION;
    }
}
