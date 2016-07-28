package org.cryse.unifystorage.explorer.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.NotificationCompat;

import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.service.operation.DownloadOperation;
import org.cryse.unifystorage.explorer.service.operation.base.Operation;
import org.cryse.unifystorage.explorer.service.operation.base.OperationState;
import org.cryse.unifystorage.explorer.service.operation.base.OperationSummary;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationHelper {
    private ConcurrentHashMap<Integer, NotificationCompat.Builder> mNotificationBuilderMap;

    private Service mService;
    private Object mNotificationLock = new Object();

    NotificationManager mNotificationManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int tokenInt = intent.getIntExtra(DataContract.Argument.OperationTokenInt, 0);
            switch (intent.getAction()) {
                case DataContract.Action.CancelOperation:
                    break;
                case DataContract.Action.ShowOperationDialog:
                    break;
            }
        }
    };

    public NotificationHelper(Service service) {
        this.mService = service;
        this.mNotificationManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationBuilderMap = new ConcurrentHashMap<>();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataContract.Action.CancelOperation);
        intentFilter.addAction(DataContract.Action.ShowOperationDialog);
        this.mService.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    public void destroy() {
        mService.unregisterReceiver(mBroadcastReceiver);
        for (Iterator<Map.Entry<Integer, NotificationCompat.Builder>> it = mNotificationBuilderMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, NotificationCompat.Builder> entry = it.next();
            cancelNotification(entry.getKey());
            it.remove();
        }
    }

    public void buildForOperation(Operation operation) {
        synchronized (mNotificationLock) {
            int tokenInt = operation.getTokenInt();
            String token = operation.getToken();
            NotificationCompat.Builder notificationBuilder = mNotificationBuilderMap.get(tokenInt);
            if(notificationBuilder == null) {
                notificationBuilder = new NotificationCompat.Builder(mService);

                Intent clickIntent = new Intent(DataContract.Action.ShowOperationDialog);
                clickIntent.putExtra(DataContract.Argument.OperationTokenInt, tokenInt);
                clickIntent.putExtra(DataContract.Argument.OperationToken, token);
                PendingIntent clickPendingIntent =
                        PendingIntent.getBroadcast(mService, 0, clickIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                Intent cancelIntent = new Intent(DataContract.Action.CancelOperation);
                cancelIntent.putExtra(DataContract.Argument.OperationTokenInt, tokenInt);
                cancelIntent.putExtra(DataContract.Argument.OperationToken, token);
                PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(mService, 1, cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT);


                mNotificationBuilderMap.put(tokenInt, notificationBuilder);
                // status.setNotificationBuilder(notificationBuilder);
                notificationBuilder
                        .setContentIntent(clickPendingIntent)
                        .addAction(R.drawable.ic_action_cancel, mService.getString(android.R.string.cancel), cancelPendingIntent);
            }

            int iconResId = R.mipmap.ic_launcher;
            String title = operation.getSummaryTitle(mService, true);
            String content = operation.getProgressDescForNotification(mService);
            int displayPercent = (int) (operation.getProgressForNotification());
            boolean indeterminate = displayPercent < 0;

            notificationBuilder.setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(iconResId).setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(content))
                    .setProgress(100, displayPercent, indeterminate)
                    .setOngoing(true);

            // startForeground(notificationId, progressNotificationBuilder.build());
            mNotificationManager.notify(tokenInt, notificationBuilder.build());
        }
    }

    public void updateNotification(Operation operation) {
        synchronized (mNotificationLock) {
            buildForOperation(operation);
        }
    }

    public void cancelNotification(Operation operation) {
        cancelNotification(operation.getTokenInt());
    }

    public void cancelNotification(int tokenInt) {
        synchronized (mNotificationLock) {
            mNotificationManager.cancel(tokenInt);
            mNotificationBuilderMap.remove(tokenInt);
        }
    }

    public void showCompletedNotification(Operation operation) {
        synchronized (mNotificationLock) {
            if ((operation.showCompletedNotification() && operation.getState() == OperationState.COMPLETED) || operation.getState() == OperationState.FAILED) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(mService);

                builder.setContentTitle(operation.getSummaryFinishedTitle(mService))
                        .setContentText(operation.getSummaryFinishedContent(mService))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true);

                PendingIntent pendingIntent = operation.getCompletedPendingIntentForNotification(mService);
                if(pendingIntent != null) {
                    builder.setContentIntent(pendingIntent);
                }

                mNotificationManager.cancel(operation.getTokenInt());
                mNotificationManager.notify(operation.getTokenInt(), builder.build());
            }
        }
    }
}
