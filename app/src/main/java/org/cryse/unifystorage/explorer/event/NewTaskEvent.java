package org.cryse.unifystorage.explorer.event;

import org.cryse.unifystorage.explorer.service.task.Task;

public class NewTaskEvent extends AbstractEvent {
    private Task mTask;

    public NewTaskEvent(Task task) {
        this.mTask = task;
    }

    public Task getTask() {
        return mTask;
    }

    @Override
    public int eventId() {
        return EventConst.EVENT_ID_NEW_TASK;
    }
}
