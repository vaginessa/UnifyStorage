package org.cryse.unifystorage.explorer.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.cryse.unifystorage.RemoteFileDownloader;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.application.StorageProviderManager;
import org.cryse.unifystorage.explorer.event.AbstractEvent;
import org.cryse.unifystorage.explorer.event.EventConst;
import org.cryse.unifystorage.explorer.event.RxEventBus;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.utils.RxSubscriptionUtils;
import org.cryse.unifystorage.utils.Path;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import okhttp3.Request;
import rx.Subscription;
import rx.functions.Action1;

public class DownloadService extends Service {
    private static final String LOG_TAG = DownloadService.class.getSimpleName();

    HandlerThread mHandlerThread;
    Handler mHandler;
    RxEventBus mEventBus = RxEventBus.getInstance();
    Subscription mEventSubscription;
    NotificationManager mNotificationManager;
    Hashtable<Integer, RemoteFileDownloader> mDownloaders = new Hashtable<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mHandlerThread = new HandlerThread(LOG_TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mEventSubscription = mEventBus.toObservable().subscribe(new Action1<AbstractEvent>() {
            @Override
            public void call(AbstractEvent abstractEvent) {
                // if(abstractEvent.eventId() == EventConst.EVENT_ID_TASK_FILE_OPERATION)
                //    onFileOperationTask((FileOperationTaskEvent)abstractEvent);
                switch (abstractEvent.eventId()) {
                    case EventConst.EVENT_ID_TASK_DOWNLOAD_FILE_START:
                        onStartDownloadFileTask((StartDownloadEvent) abstractEvent);
                        break;
                    case EventConst.EVENT_ID_TASK_DOWNLOAD_FILE_STOP:
                        onStopDownloadFileTask((StopDownloadEvent) abstractEvent);
                        break;
                }
            }
        });
    }

    private StorageProvider getStorageProvider(StorageProviderInfo storageProviderInfo) {
        return StorageProviderManager.getInstance().createStorageProvider(
                this,
                storageProviderInfo.getStorageProviderId(),
                storageProviderInfo.getCredential(),
                storageProviderInfo.getExtras()
        );
    }

    private void onStartDownloadFileTask(final StartDownloadEvent event) {
        final StorageProvider storageProvider = getStorageProvider(event.getStorageProviderInfo());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Request request = storageProvider.download(event.getRemoteFile());
                    RemoteFileDownloader downloader = new RemoteFileDownloader(event.getRemoteFile(), request);
                    mDownloaders.put(event.getDownloadToken(), downloader);

                    final File targetFile = new File(event.getSavePath());
                    String parentDirectoryPath = Path.getDirectory(event.getSavePath());
                    if (parentDirectoryPath != null) {
                        File parentDirectory = new File(parentDirectoryPath);
                        if (!parentDirectory.exists()) parentDirectory.mkdirs();
                    }
                    if (!targetFile.exists()) targetFile.createNewFile();
                    if (!targetFile.canWrite())
                        throw new IOException("Target file not writable.");

                    final Intent progressIntent = new Intent(DataContract.DOWNLOAD_BROADCAST__PROGRESS_IDENTIFIER);
                    progressIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_TOKEN, event.getDownloadToken());
                    progressIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_FILENAME, event.getRemoteFile().getName());
                    progressIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_FILE_SIZE, event.getRemoteFile().size());
                    progressIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_READ_SIZE, 0);
                    progressIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_PERCENTAGE, 0d);

                    downloader.download(targetFile, new RemoteFileDownloader.DownloadListener() {
                        @Override
                        public void downloadStart() {
                            // Send Start Download Broadcast here
                            Intent startIntent = new Intent(DataContract.DOWNLOAD_BROADCAST_START_IDENTIFIER);
                            startIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_TOKEN, event.getDownloadToken());
                            startIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_FILENAME, event.getRemoteFile().getName());
                            sendBroadcast(startIntent);
                        }

                        @Override
                        public void downloading(long readSize, long totalSize) {
                            int newPercent = (int) Math.round(((double) readSize  / (double) totalSize) * 100.0d);
                            /*if(newPercent > lastPercentage[0]) {*/
                                progressIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_READ_SIZE, readSize);
                                progressIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_PERCENTAGE, newPercent);
                                sendBroadcast(progressIntent);
                            /*    lastPercentage[0] = newPercent;
                            }*/
                        }

                        @Override
                        public void downloadFinished() {
                            Intent successIntent = new Intent(DataContract.DOWNLOAD_BROADCAST_SUCCESS_IDENTIFIER);
                            successIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_TOKEN, event.getDownloadToken());
                            successIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_FILENAME, event.getRemoteFile().getName());
                            successIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_SUCCESS_OPEN, event.isOpenAfterDownload());
                            successIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_SUCCESS_PATH, event.getSavePath());
                            sendBroadcast(successIntent);
                            if(mDownloaders.containsKey(event.getDownloadToken()))
                                mDownloaders.remove(event.getDownloadToken());
                        }

                        @Override
                        public void downloadFailed(Throwable throwable) {
                            // Send error broadcast here to dismiss progress dialog and show error message
                            Intent errorIntent = new Intent(DataContract.DOWNLOAD_BROADCAST_ERROR_IDENTIFIER);
                            errorIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_TOKEN, event.getDownloadToken());
                            errorIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_FILENAME, event.getRemoteFile().getName());
                            errorIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_ERROR_MESSAGE, throwable.getMessage());
                            sendBroadcast(errorIntent);
                            if(mDownloaders.containsKey(event.getDownloadToken()))
                                mDownloaders.remove(event.getDownloadToken());
                        }
                    });
                    /*Intent successIntent = new Intent(DataContract.DOWNLOAD_BROADCAST_SUCCESS_IDENTIFIER);
                    successIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_TOKEN, event.getDownloadToken());
                    successIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_FILENAME, event.getRemoteFile().getName());
                    sendBroadcast(successIntent);*/
                    // subscriber.onCompleted();
                } catch (IOException ex) {
                    // Send error broadcast here to dismiss progress dialog and show error message
                    Intent errorIntent = new Intent(DataContract.DOWNLOAD_BROADCAST_ERROR_IDENTIFIER);
                    errorIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_TOKEN, event.getDownloadToken());
                    errorIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_FILENAME, event.getRemoteFile().getName());
                    errorIntent.putExtra(DataContract.DOWNLOAD_BROADCAST_ERROR_MESSAGE, ex.getMessage());
                    sendBroadcast(errorIntent);
                } finally {
                    // IOUtils.closeQuietly(downloadStream);
                }
            }
        });
    }

    private void onStopDownloadFileTask(StopDownloadEvent event) {
        if(mDownloaders.containsKey(event.getDownloadToken())) {
            RemoteFileDownloader downloader = mDownloaders.get(event.getDownloadToken());
            downloader.cancel();
            mDownloaders.remove(event.getDownloadToken());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxSubscriptionUtils.checkAndUnsubscribe(mEventSubscription);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            mHandlerThread.quitSafely();
        else
            mHandlerThread.quit();
        for(RemoteFileDownloader downloader : mDownloaders.values()) {
            if(downloader != null) {
                downloader.cancel();
            }
        }
        mDownloaders.clear();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadServiceBinder(this);
    }

    public static class DownloadServiceBinder extends Binder {
        private DownloadService mService;

        public DownloadServiceBinder(DownloadService service) {
            this.mService = service;
        }
    }
}
