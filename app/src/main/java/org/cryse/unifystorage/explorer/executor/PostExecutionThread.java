package org.cryse.unifystorage.explorer.executor;

import rx.Scheduler;

public interface PostExecutionThread {
    Scheduler getScheduler();
}
