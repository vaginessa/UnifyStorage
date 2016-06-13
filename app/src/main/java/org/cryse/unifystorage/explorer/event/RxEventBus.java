package org.cryse.unifystorage.explorer.event;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public enum RxEventBus {
    INSTANCE;

    private final Subject<AbstractEvent, AbstractEvent> mInstance = new SerializedSubject<AbstractEvent, AbstractEvent>(PublishSubject.<AbstractEvent>create());

    public static RxEventBus instance() {
        return INSTANCE;
    }

    public void sendEvent(AbstractEvent event) {
        mInstance.onNext(event);
    }

    public Observable<AbstractEvent> toObservable() {
        return mInstance;
    }
}
