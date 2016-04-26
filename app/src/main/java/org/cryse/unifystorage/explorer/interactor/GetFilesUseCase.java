package org.cryse.unifystorage.explorer.interactor;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.RxStorageProvider;
import org.cryse.unifystorage.explorer.executor.PostExecutionThread;
import org.cryse.unifystorage.explorer.executor.ThreadExecutor;
import org.cryse.unifystorage.utils.DirectoryInfo;

import rx.Observable;
import rx.functions.Func1;

public class GetFilesUseCase extends UseCase<GetFilesUseCase.RequestValues, UseCase.SingleResponseValue<DirectoryInfo>> {
    private final RxStorageProvider rxStorageProvider;

    public GetFilesUseCase(RxStorageProvider rxStorageProvider, ThreadExecutor threadExecutor,
                    PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.rxStorageProvider = rxStorageProvider;
    }

    @Override public Observable<SingleResponseValue<DirectoryInfo>> buildUseCaseObservable(RequestValues requestValues) {
        Observable<DirectoryInfo> observable;
        if(requestValues.parentFile == null)
            observable = this.rxStorageProvider.list();
        else
            observable = this.rxStorageProvider.list(requestValues.parentFile);
        return observable.map(new Func1<DirectoryInfo, SingleResponseValue<DirectoryInfo>>() {
            @Override
            public SingleResponseValue<DirectoryInfo> call(DirectoryInfo directoryInfo) {
                return new SingleResponseValue<>(directoryInfo);
            }
        });
    }

    public static class RequestValues extends UseCase.RequestValues {
        public final RemoteFile parentFile;

        public RequestValues(RemoteFile parentFile) {
            this.parentFile = parentFile;
        }
    }
}
