package org.cryse.unifystorage.explorer.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.event.AbstractEvent;
import org.cryse.unifystorage.explorer.event.CancelTaskEvent;
import org.cryse.unifystorage.explorer.event.EventConst;
import org.cryse.unifystorage.explorer.event.FrontUIDismissEvent;
import org.cryse.unifystorage.explorer.event.NewTaskEvent;
import org.cryse.unifystorage.explorer.event.RxEventBus;
import org.cryse.unifystorage.explorer.event.ShowProgressEvent;
import org.cryse.unifystorage.explorer.service.operation.CreateFolderOperation;
import org.cryse.unifystorage.explorer.service.operation.DeleteOperation;
import org.cryse.unifystorage.explorer.service.operation.DownloadOperation;
import org.cryse.unifystorage.explorer.service.operation.OnRemoteOperationListener;
import org.cryse.unifystorage.explorer.service.operation.Operation;
import org.cryse.unifystorage.explorer.service.operation.OperationObserverManager;
import org.cryse.unifystorage.explorer.service.operation.OperationStatus;
import org.cryse.unifystorage.explorer.service.operation.RemoteOperationResult;
import org.cryse.unifystorage.explorer.service.task.RemoteTask;
import org.cryse.unifystorage.explorer.service.task.Task;
import org.cryse.unifystorage.explorer.utils.RxSubscriptionUtils;
import org.cryse.unifystorage.utils.FileSizeUtils;

import java.util.concurrent.ConcurrentHashMap;

import rx.Subscription;
import rx.functions.Action1;

public class OperationService extends Service {
    private static final String LOG_TAG = OperationService.class.getSimpleName();
    private ConcurrentHashMap<String, OperationStatus> mOperationStatusMap;
    RxEventBus mEventBus = RxEventBus.instance();
    Subscription mEventSubscription;
    Handler mOperationListenerHandler;
    NotificationManager mNotifyManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mOperationListenerHandler = new Handler();
        mOperationStatusMap = new ConcurrentHashMap<>();
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
        OperationStatus status = mOperationStatusMap.get(token);
        if(status != null) {
            status.setShouldShowNotification(true);
        } else {

        }
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

    public void showProgressDialog(String token) {
        OperationStatus status = mOperationStatusMap.get(token);
        if(status != null) {
            RxEventBus.instance().sendEvent(new ShowProgressEvent(status.getOperation()));
        }
    }

    public void showNotificationForOperation(OperationStatus status) {
        NotificationCompat.Builder notificationBuilder = status.getNotificationBuilder();
        if(notificationBuilder == null) {
            notificationBuilder = new NotificationCompat.Builder(OperationService.this);

            Intent clickIntent = new Intent(this, OperationService.class);
            clickIntent.setAction("show_progress_dialog");
            clickIntent.putExtra("type", "notification_action");
            clickIntent.putExtra("token", status.getToken());
            PendingIntent clickPendingIntent =
                    PendingIntent.getService(this, 0, clickIntent, 0);

            Intent cancelIntent = new Intent(this, OperationService.class);
            cancelIntent.setAction("cancel_task");
            cancelIntent.putExtra("type", "notification_action");
            cancelIntent.putExtra("token", status.getToken());
            PendingIntent cancelPendingIntent = PendingIntent.getService(this, 1, cancelIntent, 0);

            Intent cancel2Intent = new Intent(this, OperationService.class);
            cancel2Intent.setAction("asdf");
            cancel2Intent.putExtra("type", "notification_action");
            cancel2Intent.putExtra("token", status.getToken());
            PendingIntent cancel2PendingIntent = PendingIntent.getService(this, 2, cancel2Intent, 0);


            status.setNotificationBuilder(notificationBuilder);
            notificationBuilder
                    .setContentIntent(clickPendingIntent)
                    .addAction(R.drawable.ic_action_cancel, getString(R.string.dialog_button_cancel), cancelPendingIntent)
                    .addAction(R.drawable.ic_action_archive, getString(R.string.dialog_title_running), cancel2PendingIntent);
        }

        Operation operation = status.getOperation();
        int notificationId = status.getTokenInt();
        int iconResId = R.mipmap.ic_launcher;
        String title = getString(R.string.dialog_title_running);
        String content = getString(R.string.dialog_content_please_wait);
        long current = status.getCurrent();
        long total = status.getTotal();
        int newPercent = (int) Math.round(((double) current / (double) total) * 100.0d);
        boolean indeterminate = true;
        if(operation instanceof DownloadOperation) {
            title = getString(R.string.dialog_title_downloading_file);
            content = getString(
                    R.string.dialog_content_download_file_progress,
                    FileSizeUtils.humanReadableByteCount(current, false),
                    FileSizeUtils.humanReadableByteCount(total, false)
            );
            indeterminate = false;
        } else if(operation instanceof DeleteOperation) {
            title = getString(R.string.dialog_title_deleting_file);
            content = getString(R.string.dialog_content_delete_file_progress, current, total);
        } else if(operation instanceof CreateFolderOperation) {
            title = getString(R.string.dialog_title_downloading_file);
            content = getString(R.string.dialog_title_creating_directory);
        } else {

        }

        notificationBuilder.setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(iconResId).setStyle(new NotificationCompat.BigTextStyle()
                .bigText(content))
                .setProgress(100, newPercent, indeterminate)
                .setOngoing(true);

        // startForeground(notificationId, progressNotificationBuilder.build());
        mNotifyManager.notify(notificationId, notificationBuilder.build());
    }

    private void cancelNotificationForOperation(OperationStatus status) {
        mNotifyManager.cancel(status.getTokenInt());
        status.setShouldShowNotification(false);
        status.setNotificationBuilder(null);
        /*if(mOperationStatusMap.isEmpty())
            stopForeground(true);*/
    }

    private OnRemoteOperationListener mOnRemoteOperationListener = new OnRemoteOperationListener() {
        @Override
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
