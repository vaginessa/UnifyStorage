package org.cryse.unifystorage.explorer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.cryse.unifystorage.explorer.event.AbstractEvent;
import org.cryse.unifystorage.explorer.event.CancelTaskEvent;
import org.cryse.unifystorage.explorer.event.EventConst;
import org.cryse.unifystorage.explorer.event.FrontUIDismissEvent;
import org.cryse.unifystorage.explorer.event.NewTaskEvent;
import org.cryse.unifystorage.explorer.event.RxEventBus;
import org.cryse.unifystorage.explorer.event.ShowProgressEvent;
import org.cryse.unifystorage.explorer.service.operation.base.OnOperationListener;
import org.cryse.unifystorage.explorer.service.operation.base.Operation;
import org.cryse.unifystorage.explorer.service.operation.base.OperationState;
import org.cryse.unifystorage.explorer.service.task.RemoteTask;
import org.cryse.unifystorage.explorer.service.task.Task;
import org.cryse.unifystorage.explorer.utils.RxSubscriptionUtils;

import java.util.concurrent.ConcurrentHashMap;

import rx.Subscription;
import rx.functions.Action1;

public class OperationService extends Service {
    private static final String LOG_TAG = OperationService.class.getSimpleName();
    private ConcurrentHashMap<String, Operation> mOperationMap;
    RxEventBus mEventBus = RxEventBus.instance();
    Subscription mEventSubscription;
    Handler mOperationListenerHandler;
    NotificationHelper mNotificationHelper;

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int tokenInt = intent.getIntExtra("tokenInt", 0);
            String token = intent.getStringExtra("token");

            Operation operation = mOperationMap.get(token);
            if(operation != null) {
                switch (intent.getAction()) {
                    case "org.cryse.unifystorage.ACTION_CANCEL_OPERATION":
                        operation.cancel();
                        break;
                    case "org.cryse.unifystorage.ACTION_PAUSE_OPERATION":
                            try {
                                operation.pause();
                            } catch (InterruptedException e) {
                                Log.e("ABC", e.getMessage(), e);
                            }
                        break;
                }
            }

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mOperationListenerHandler = new Handler();
        mOperationMap = new ConcurrentHashMap<>();
        mNotificationHelper = new NotificationHelper(this);
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
                    case EventConst.EVENT_ID_FRONT_UI_DISMISS:
                        onFrontUIDismissEvent((FrontUIDismissEvent) abstractEvent);
                        break;
                }
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("org.cryse.unifystorage.ACTION_DISMISS_OPERATION");
        intentFilter.addAction("org.cryse.unifystorage.ACTION_CANCEL_OPERATION");
        intentFilter.addAction("org.cryse.unifystorage.ACTION_PAUSE_OPERATION");
        intentFilter.addAction("org.cryse.unifystorage.ACTION_SHOW_FRONT_OPERATION");
        this.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxSubscriptionUtils.checkAndUnsubscribe(mEventSubscription);
        unregisterReceiver(mBroadcastReceiver);
        mNotificationHelper.destroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new OperationBinder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        if(intent != null && intent.hasExtra("type")) {

            Log.d(LOG_TAG, String.format("onStartCommand: %s", intent.getStringExtra("type")));
            if("notification_action".compareTo(intent.getStringExtra("type")) == 0) {
                String actionName = intent.getAction();
                String token = intent.getStringExtra("token");
                switch (actionName) {
                    case "cancel_task":
                        cancelOperation(token);
                        break;
                    case "show_progress_dialog":
                        showProgressDialog(token);
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void onNewTaskEvent(NewTaskEvent event) {
        newTask(event.getTask());
    }

    private void onCancelTaskEvent(CancelTaskEvent event) {
        String token = event.getToken();
        cancelOperation(token);
    }

    private void onFrontUIDismissEvent(FrontUIDismissEvent event) {
        String token = event.getToken();
        /*OperationStatus status = mOperationMap.get(token);
        if(status != null) {
            status.setShouldShowNotification(true);
        } else {

        }*/
    }

    private String newTask(Task task) {
        String token = task.generateToken();
        Operation operation = mOperationMap.get(token);
        if(operation == null) {
            operation = task.getOperation(this, mOnOperationListener, mOperationListenerHandler);
            mOperationMap.put(token, operation);
            if(task.shouldQueue()) {
                operation.executeInBackground();
            } else {
                operation.executeInBackground();
            }
        }
        return token;
    }

    public void cancelOperation(String token) {
        Operation operation = mOperationMap.get(token);
        if(operation != null) {
            operation.cancel();
        }
    }

    public void showProgressDialog(String token) {
        Operation operation = mOperationMap.get(token);
        if(operation != null) {
            RxEventBus.instance().sendEvent(new ShowProgressEvent(operation));
        }
    }

    private OnOperationListener mOnOperationListener = new OnOperationListener() {
        @Override
        public void onOperationStateChanged(Operation operation, OperationState state) {
            switch (state) {
                case NEW:
                    mNotificationHelper.buildForOperation(operation);
                    break;
                case RUNNING:
                    break;
                case COMPLETED:
                    mNotificationHelper.cancelNotification(operation);
                    mOperationMap.remove(operation.getToken());
                    mNotificationHelper.showCompletedNotification(operation);
                    break;
                case FAILED:
                    mNotificationHelper.cancelNotification(operation);
                    mOperationMap.remove(operation.getToken());
                    mNotificationHelper.showCompletedNotification(operation);
                    break;
            }
        }

        @Override
        public void onOperationProgress(Operation operation, long currentItemRead, long currentItemSize, long currentSpeed, long itemIndex, long itemCount, long totalRead, long totalSize) {
            mNotificationHelper.updateNotification(operation);
        }

        /*@Override
        public void onRemoteOperationStart(Operation operation) {
            String token = operation.getOperationToken();
            OperationStatus status = mOperationStatusMap.get(token);
            if(status != null && status.shouldShowNotification()) {
                showNotificationForOperation(status);
            }
            OperationObserverManager.instance().onRemoteOperationStart(operation);
        }

        @Override
        public void onRemoteOperationFinish(Operation operation, RemoteOperationResult result) {
            String token = operation.getOperationToken();
            if(mOperationStatusMap.containsKey(token)) {
                OperationStatus status = mOperationStatusMap.get(token);
                status.cancel();
                if(status.shouldShowNotification())
                    cancelNotificationForOperation(status);
                mOperationStatusMap.remove(token);
            }
            OperationObserverManager.instance().onRemoteOperationFinish(operation, result);
        }

        @Override
        public void onRemoteOperationProgress(Operation operation, long current, long total) {
            String token = operation.getOperationToken();
            OperationObserverManager.instance().onRemoteOperationProgress(operation, current, total);
            OperationStatus status = mOperationStatusMap.get(token);
            if(status != null) {
                status.setProgress(current, total);
                if(status.shouldShowNotification()) {
                    showNotificationForOperation(status);
                }
            }
        }*/
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
