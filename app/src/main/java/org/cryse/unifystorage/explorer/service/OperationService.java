package org.cryse.unifystorage.explorer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.cryse.unifystorage.explorer.event.AbstractEvent;
import org.cryse.unifystorage.explorer.event.CancelTaskEvent;
import org.cryse.unifystorage.explorer.event.EventConst;
import org.cryse.unifystorage.explorer.event.NewTaskEvent;
import org.cryse.unifystorage.explorer.event.RxEventBus;
import org.cryse.unifystorage.explorer.service.operation.OnRemoteOperationListener;
import org.cryse.unifystorage.explorer.service.operation.Operation;
import org.cryse.unifystorage.explorer.service.operation.OperationObserverManager;
import org.cryse.unifystorage.explorer.service.operation.OperationStatus;
import org.cryse.unifystorage.explorer.service.operation.RemoteOperation;
import org.cryse.unifystorage.explorer.service.operation.RemoteOperationResult;
import org.cryse.unifystorage.explorer.service.task.RemoteTask;
import org.cryse.unifystorage.explorer.service.task.Task;
import org.cryse.unifystorage.explorer.utils.RxSubscriptionUtils;

import java.util.concurrent.ConcurrentHashMap;

import rx.Subscription;
import rx.functions.Action1;

public class OperationService extends Service {
    private ConcurrentHashMap<String, OperationStatus> mOperationStatusMap;
    RxEventBus mEventBus = RxEventBus.getInstance();
    Subscription mEventSubscription;
    Handler mOperationListenerHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mOperationListenerHandler = new Handler();
        mOperationStatusMap = new ConcurrentHashMap<>();
        mEventSubscription = mEventBus.toObservable().subscribe(new Action1<AbstractEvent>() {
            @Override
            public void call(AbstractEvent abstractEvent) {
                switch (abstractEvent.eventId()) {
                    case EventConst.EVENT_ID_NEW_TASK:
                        onNewTaskEvent((NewTaskEvent)abstractEvent);
                        break;
                    case EventConst.EVENT_ID_CANCEL_TASK:
                        onCancelTaskEvent((CancelTaskEvent)abstractEvent);
                        break;
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxSubscriptionUtils.checkAndUnsubscribe(mEventSubscription);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new OperationBinder(this);
    }

    private void onNewTaskEvent(NewTaskEvent event) {
        newTask(event.getTask());
    }

    private void onCancelTaskEvent(CancelTaskEvent event) {
        String token = event.getToken();
        cancelOperation(token);
    }

    private String newTask(Task task) {
        Operation operation = task.getOperation(this);
        String operationToken = operation.getOperationToken();
        Operation.OperationContext operationContext = task.getOperationContext(this);
        if(task.shouldQueue()) {
            // Add to Queue
            // Pair<RemoteTask, RemoteOperation> taskPair = Pair.create(task, operation);
            Thread thread = operation.executeAsync(operationContext, mOnRemoteOperationListener, mOperationListenerHandler);
            OperationStatus status = new OperationStatus.Builder()
                    .token(operationToken)
                    .operation(operation)
                    .workThread(thread)
                    .showNotification(false)
                    .build();
            mOperationStatusMap.put(operationToken, status);
        } else {
            // Run async parallel
            Thread thread = operation.executeAsync(operationContext, mOnRemoteOperationListener, mOperationListenerHandler);
            OperationStatus status = new OperationStatus.Builder()
                    .token(operationToken)
                    .operation(operation)
                    .workThread(thread)
                    .showNotification(false)
                    .build();
            mOperationStatusMap.put(operationToken, status);
        }
        return operationToken;
    }

    public void cancelOperation(String token) {
        OperationStatus status = mOperationStatusMap.get(token);
        if(status != null) {
            status.cancel();
        }
    }

    private OnRemoteOperationListener mOnRemoteOperationListener = new OnRemoteOperationListener() {
        @Override
        public void onRemoteOperationStart(RemoteOperation caller) {
            String token = caller.getOperationToken();
            OperationObserverManager.instance().onRemoteOperationStart(caller);
        }

        @Override
        public void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result) {
            String token = caller.getOperationToken();
            if(mOperationStatusMap.containsKey(token)) {
                OperationStatus status = mOperationStatusMap.get(token);
                status.cancel();
                mOperationStatusMap.remove(token);
            }
            OperationObserverManager.instance().onRemoteOperationFinish(caller, result);
        }

        @Override
        public void onRemoteOperationProgress(RemoteOperation caller, long current, long total) {
            String token = caller.getOperationToken();
            OperationObserverManager.instance().onRemoteOperationProgress(caller, current, total);
        }
    };

    public static class OperationBinder extends Binder {
        OperationService mService;
        public OperationBinder(OperationService service) {
            mService = service;
        }

        public String newTask(RemoteTask task) {
            return mService.newTask(task);
        }

        public void cancelTask(String token) {
            mService.cancelOperation(token);
        }
    }
}
