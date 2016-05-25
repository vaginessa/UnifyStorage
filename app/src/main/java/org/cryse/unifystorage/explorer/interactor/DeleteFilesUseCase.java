package org.cryse.unifystorage.explorer.interactor;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.event.RxEventBus;
import org.cryse.unifystorage.explorer.executor.PostExecutionThread;
import org.cryse.unifystorage.explorer.executor.ThreadExecutor;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.service.FileOperation;
import org.cryse.unifystorage.explorer.service.FileOperationTaskEvent;
import org.cryse.unifystorage.explorer.utils.RandomUtils;

import rx.Observable;
import rx.Subscriber;

public class DeleteFilesUseCase extends UseCase<DeleteFilesUseCase.RequestValues, UseCase.SingleResponseValue<Void>> {
    protected RxEventBus mRxEventBus;
    public DeleteFilesUseCase(RxEventBus rxEventBus,
                                 ThreadExecutor threadExecutor,
                                 PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.mRxEventBus = rxEventBus;
    }

    @Override
    protected Observable<SingleResponseValue<Void>> buildUseCaseObservable(final RequestValues requestValues) {
        return Observable.create(new Observable.OnSubscribe<SingleResponseValue<Void>>() {
            @Override
            public void call(Subscriber<? super SingleResponseValue<Void>> subscriber) {
                mRxEventBus.sendEvent(
                        new FileOperationTaskEvent(
                                new FileOperation(
                                        FileOperation.FileOperationCode.DELETE,
                                        RandomUtils.nextInt(),
                                        requestValues.getStorageProviderInfo(),
                                        requestValues.getDirectory(),
                                        requestValues.getFiles()
                                )
                        )
                );
                subscriber.onCompleted();
            }
        });
    }

    public static class RequestValues extends UseCase.RequestValues {
        public StorageProviderInfo storageProviderInfo;
        public RemoteFile directory;
        public RemoteFile[] files;

        public RequestValues(StorageProviderInfo storageProviderInfo, RemoteFile directory, RemoteFile[] files) {
            this.storageProviderInfo = storageProviderInfo;
            this.directory = directory;
            this.files = files;
        }

        public StorageProviderInfo getStorageProviderInfo() {
            return storageProviderInfo;
        }

        public RemoteFile getDirectory() {
            return directory;
        }

        public RemoteFile[] getFiles() {
            return files;
        }
    }
}
