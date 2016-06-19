package org.cryse.unifystorage.explorer.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.NotificationCompat;

import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.service.operation.OperationSummary;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationHelper {
    private ConcurrentHashMap<Integer, NotificationCompat.Builder> mNotificationBuilderMap;

    private Service mService;
    NotificationManager mNotificationManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int tokenInt = intent.getIntExtra("token", 0);
            switch (intent.getAction()) {
                case "org.cryse.unifystorage.ACTION_DISMISS_OPERATION":
                    break;
                case "org.cryse.unifystorage.ACTION_PAUSE_OPERATION":
                    break;
                case "org.cryse.unifystorage.ACTION_CANCEL_OPERATION":
                    break;
            }
        }
    };

    public NotificationHelper(Service service) {
        this.mService = service;
        this.mNotificationManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationBuilderMap = new ConcurrentHashMap<>();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("org.cryse.unifystorage.ACTION_DISMISS_OPERATION");
        intentFilter.addAction("org.cryse.unifystorage.ACTION_CANCEL_OPERATION");
        intentFilter.addAction("org.cryse.unifystorage.ACTION_PAUSE_OPERATION");
        intentFilter.addAction("org.cryse.unifystorage.ACTION_SHOW_FRONT_OPERATION");
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

    public void buildForSummary(OperationSummary summary) {
        int notificationId = summary.tokenInt;
        NotificationCompat.Builder notificationBuilder = mNotificationBuilderMap.get(notificationId);
        if(notificationBuilder == null) {
            notificationBuilder = new NotificationCompat.Builder(mService);

            Intent clickIntent = new Intent("org.cryse.unifystorage.ACTION_PAUSE_OPERATION");
            clickIntent.putExtra("type", "notification_action");
            clickIntent.putExtra("tokenInt", notificationId);
            clickIntent.putExtra("token", summary.token);
            PendingIntent clickPendingIntent =
                    PendingIntent.getBroadcast(mService, 0, clickIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            Intent cancelIntent = new Intent("org.cryse.unifystorage.ACTION_CANCEL_OPERATION");
            cancelIntent.putExtra("type", "notification_action");
            cancelIntent.putExtra("tokenInt", notificationId);
            cancelIntent.putExtra("token", summary.token);
            PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(mService, 1, cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT);


            mNotificationBuilderMap.put(notificationId, notificationBuilder);
            // status.setNotificationBuilder(notificationBuilder);
            notificationBuilder
                    .setContentIntent(clickPendingIntent)
                    .addAction(R.drawable.ic_action_cancel, mService.getString(R.string.dialog_button_cancel), cancelPendingIntent);
        }

        int iconResId = R.mipmap.ic_launcher;
        String title = summary.title.get();
        String content = summary.simpleContent.get();
        int displayPercent = (int) (summary.displayPercent * 100.0d);
        boolean indeterminate = false;

        notificationBuilder.setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(iconResId).setStyle(new NotificationCompat.BigTextStyle()
                .bigText(content))
                .setProgress(100, displayPercent, indeterminate)
                .setOngoing(true);

        // startForeground(notificationId, progressNotificationBuilder.build());
        mNotificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void updateNotification(OperationSummary summary) {
        buildForSummary(summary);
    }

    public void cancelNotification(OperationSummary summary) {
        cancelNotification(summary.tokenInt);
    }

    public void cancelNotification(int tokenInt) {
        mNotificationManager.cancel(tokenInt);
        mNotificationBuilderMap.remove(tokenInt);
    }
}
