package org.cryse.unifystorage.explorer.interactor;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.RxStorageProvider;
import org.cryse.unifystorage.explorer.executor.PostExecutionThread;
import org.cryse.unifystorage.explorer.executor.ThreadExecutor;

import rx.Observable;
import rx.functions.Func1;

public class GetFileUseCase extends UseCase<GetFileUseCase.RequestValues, UseCase.SingleResponseValue<RemoteFile>> {

    private final RxStorageProvider rxStorageProvider;

    public GetFileUseCase(RxStorageProvider rxStorageProvider, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.rxStorageProvider = rxStorageProvider;
    }

    @Override
    protected Observable<SingleResponseValue<RemoteFile>> buildUseCaseObservable(RequestValues requestValues) {
        return rxStorageProvider.getFile(requestValues.path).map(new Func1<RemoteFile, SingleResponseValue<RemoteFile>>() {
            @Override
            public SingleResponseValue<RemoteFile> call(RemoteFile file) {
                return new SingleResponseValue<>(file);
            }
        });
    }

    public static class RequestValues extends UseCase.RequestValues {
        public final String path;

        public RequestValues(RemoteFile file) {
            this.path = file.getPath();
        }

        public RequestValues(String path) {
            this.path = path;
        }
    }
}
