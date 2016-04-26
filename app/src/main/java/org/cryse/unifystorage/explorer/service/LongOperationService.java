package org.cryse.unifystorage.explorer.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.cryse.unifystorage.RxStorageProvider;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.application.StorageProviderManager;
import org.cryse.unifystorage.explorer.event.FileDeleteEvent;
import org.cryse.unifystorage.explorer.event.FileDeleteResultEvent;
import org.cryse.unifystorage.explorer.event.RxEventBus;
import org.cryse.unifystorage.utils.OperationResult;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LongOperationService extends Service {
    RxEventBus mEventBus = RxEventBus.getInstance();
    NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LongOperationBinder(this);
    }

    public void doOperation(FileOperation fileOperation) {
        FileOperation.FileOperationCode code = fileOperation.getCode();
        switch (code) {
            case COPY:
                doCopy(fileOperation);
                break;
            case MOVE:
                doMove(fileOperation);
                break;
            case DELETE:
                doDelete(fileOperation);
                break;
            case RENAME:
                doRename(fileOperation);
                break;
            case UPLOAD:
                doUpload(fileOperation);
                break;
            case DOWNLOAD:
                doDownload(fileOperation);
                break;
            case COMPRESS:
                doCompress(fileOperation);
                break;
            case UNCOMPRESS:
                doUncompress(fileOperation);
                break;
        }
    }
    private void doCopy(final FileOperation fileOperation) {
        StorageProvider storageProvider = StorageProviderManager.getInstance().createStorageProvider(
                this,
                fileOperation.getStorageProviderInfo().id,
                fileOperation.getStorageProviderInfo().credential,
                fileOperation.getStorageProviderInfo().extras
        );
        if(storageProvider == null) throw new IllegalStateException("Cannot get StorageProvider instance");
        RxStorageProvider rxStorageProvider = new RxStorageProvider(storageProvider);
        final int fileCount = fileOperation.getFiles().length;
        final int[] currentProgress = new int[]{0};

        rxStorageProvider.copyFiles(fileOperation.getTarget(), fileOperation.getFiles()).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<OperationResult>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        String resultToast = String.format("Copy failed: %s", e.getMessage());
                        Log.e("CopyFile", resultToast);
                    }

                    @Override
                    public void onNext(OperationResult result) {
                        String resultToast = String.format("Copy %s %s", result.first.getName(), result.second ? "success" : "failed");
                        Log.e("CopyFile", resultToast);
                        currentProgress[0]++;
                    }
                });
    }

    private void doDelete(final FileOperation fileOperation) {
        final NotificationCompat.Builder deleteNotificationBuilder = new NotificationCompat.Builder(this);
        StorageProvider storageProvider = StorageProviderManager.getInstance().createStorageProvider(
                this,
                fileOperation.getStorageProviderInfo().id,
                fileOperation.getStorageProviderInfo().credential,
                fileOperation.getStorageProviderInfo().extras
        );
        if(storageProvider == null) throw new IllegalStateException("Cannot get StorageProvider instance");
        RxStorageProvider rxStorageProvider = new RxStorageProvider(storageProvider);
        final int fileCount = fileOperation.getFiles().length;
        final int[] currentProgress = new int[]{0};
        deleteNotificationBuilder.setContentTitle(getResources().getString(R.string.notification_title_deleting_files))
                .setContentText("")
                .setSmallIcon(R.drawable.ic_action_delete)
                .setOngoing(true);

        startForeground(fileOperation.getOperationId(), deleteNotificationBuilder.build());

        rxStorageProvider.deleteFile(fileOperation.getFiles()).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<OperationResult>() {
                    @Override
                    public void onCompleted() {
                        mNotificationManager.cancel(fileOperation.getOperationId());
                        stopForeground(true);
                        mEventBus.sendEvent(new FileDeleteResultEvent(
                                fileOperation.getStorageProviderInfo().id,
                                fileOperation.getTarget().getId(),
                                true,
                                "",
                                ""
                        ));
                    }

                    @Override
                    public void onError(Throwable e) {
                        String resultToast = String.format("Delete failed: %s", e.getMessage());
                        Log.e("DeleteFile", resultToast);
                        mNotificationManager.cancel(fileOperation.getOperationId());
                        stopForeground(true);
                        mEventBus.sendEvent(new FileDeleteResultEvent(
                                fileOperation.getStorageProviderInfo().id,
                                fileOperation.getTarget().getId(),
                                false,
                                "",
                                e.getMessage()
                        ));
                    }

                    @Override
                    public void onNext(OperationResult result) {
                        String resultToast = String.format("Delete %s %s", result.first.getName(), result.second ? "success" : "failed");
                        Log.e("DeleteFile", resultToast);
                        mEventBus.sendEvent(new FileDeleteEvent(
                                fileOperation.getStorageProviderInfo().id,
                                fileOperation.getTarget().getId(),
                                currentProgress[0],
                                fileCount,
                                result.first.getId(),
                                result.first.getName(),
                                result.second
                        ));
                        deleteNotificationBuilder
                                .setProgress(fileCount, currentProgress[0], false)
                                .setContentText(getString(R.string.notification_content_deleting_files, currentProgress[0], fileCount));
                        mNotificationManager.notify(fileOperation.getOperationId(), deleteNotificationBuilder.build());
                        currentProgress[0]++;
                    }
                });
    }

    private void doMove(FileOperation fileOperation) {

    }

    private void doRename(FileOperation fileOperation) {

    }

    private void doUpload(FileOperation fileOperation) {

    }

    private void doDownload(FileOperation fileOperation) {

    }

    private void doCompress(FileOperation fileOperation) {

    }

    private void doUncompress(FileOperation fileOperation) {

    }

    public static class LongOperationBinder extends Binder {
        private LongOperationService mService;
        public LongOperationBinder(LongOperationService service) {
            this.mService = service;
        }

        public void doOperation(FileOperation fileOperation) {
            mService.doOperation(fileOperation);
        }
    }
}
