package org.cryse.unifystorage.explorer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.RxStorageProvider;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.application.StorageProviderManager;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LongOperationService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LongOperationBinder(this);
    }

    public <RF extends RemoteFile, CR extends Credential, SP extends StorageProvider<RF, CR>>
    void doOperation(FileOperation<RF> fileOperation, Class<RF> rfClass) {
        FileOperation.FileOperationCode code = fileOperation.getCode();
        switch (code) {
            case COPY:
                doCopy(fileOperation, rfClass);
                break;
            case MOVE:
                doMove(fileOperation, rfClass);
                break;
            case DELETE:
                doDelete(fileOperation, rfClass);
                break;
            case RENAME:
                doRename(fileOperation, rfClass);
                break;
            case UPLOAD:
                doUpload(fileOperation, rfClass);
                break;
            case DOWNLOAD:
                doDownload(fileOperation, rfClass);
                break;
            case COMPRESS:
                doCompress(fileOperation, rfClass);
                break;
            case UNCOMPRESS:
                doUncompress(fileOperation, rfClass);
                break;
        }
    }

    private <RF extends RemoteFile> void doCopy(FileOperation fileOperation, Class<RF> rfClass) {

    }

    private <RF extends RemoteFile, CR extends Credential, SP extends StorageProvider<RF, CR>>
    void doDelete(FileOperation<RF> fileOperation, Class<RF> rfClass) {
        SP storageProvider = StorageProviderManager.getInstance().<RF, CR, SP>loadStorageProvider(fileOperation.getStorageProviderId());
        if(storageProvider == null) throw new IllegalStateException("Cannot get StorageProvider instance");
        RxStorageProvider<RF, CR, SP> rxStorageProvider = new RxStorageProvider<>(storageProvider);
        rxStorageProvider.deleteFile(fileOperation.getFiles()).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Pair<RF, Boolean>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        String resultToast = String.format("Delete failed: %s", e.getMessage());
                        Log.e("DeleteFile", resultToast);
                        //Toast.makeText(mContext, resultToast, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Pair<RF, Boolean> result) {
                        String resultToast = String.format("Delete %s %s", result.first.getName(), result.second ? "success" : "failed");
                        Log.e("DeleteFile", resultToast);
                        //Toast.makeText(mContext, resultToast, Toast.LENGTH_SHORT).show();
                        /*int position = 0;
                        for (Iterator<RF> iterator = mDirectory.files.iterator(); iterator.hasNext();) {
                            RF rf = iterator.next();
                            if (rf.getId().compareTo(result.first.getId()) == 0 && result.second) {
                                // Remove the current element from the iterator and the list.
                                iterator.remove();
                                if(mDataListener != null) {
                                    mDataListener.onDirectoryItemDelete(position);
                                }
                            }
                            position++;
                        }*/
                        // TODO: Use EventBus to notify list update.
                    }
                });
    }

    private <RF extends RemoteFile> void doMove(FileOperation fileOperation, Class<RF> rfClass) {

    }

    private <RF extends RemoteFile> void doRename(FileOperation fileOperation, Class<RF> rfClass) {

    }

    private <RF extends RemoteFile> void doUpload(FileOperation fileOperation, Class<RF> rfClass) {

    }

    private <RF extends RemoteFile> void doDownload(FileOperation fileOperation, Class<RF> rfClass) {

    }

    private <RF extends RemoteFile> void doCompress(FileOperation fileOperation, Class<RF> rfClass) {

    }

    private <RF extends RemoteFile> void doUncompress(FileOperation fileOperation, Class<RF> rfClass) {

    }

    public static class LongOperationBinder extends Binder {
        private LongOperationService mService;
        public LongOperationBinder(LongOperationService service) {
            this.mService = service;
        }

        public <RF extends RemoteFile, CR extends Credential, SP extends StorageProvider<RF, CR>>
        void doOperation(FileOperation<RF> fileOperation, Class<RF> rfClass) {
            mService.doOperation(fileOperation, rfClass);
        }
    }
}
