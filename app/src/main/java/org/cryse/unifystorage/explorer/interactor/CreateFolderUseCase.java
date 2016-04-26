package org.cryse.unifystorage.explorer.interactor;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.RxStorageProvider;
import org.cryse.unifystorage.explorer.executor.PostExecutionThread;
import org.cryse.unifystorage.explorer.executor.ThreadExecutor;

import rx.Observable;
import rx.functions.Func1;

public class CreateFolderUseCase extends UseCase<CreateFolderUseCase.RequestValues, UseCase.SingleResponseValue<RemoteFile>> {


    private final RxStorageProvider rxStorageProvider;

    public CreateFolderUseCase(RxStorageProvider rxStorageProvider, ThreadExecutor threadExecutor,
                           PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.rxStorageProvider = rxStorageProvider;
    }

    @Override
    public Observable<SingleResponseValue<RemoteFile>> buildUseCaseObservable(RequestValues requestValues) {
        return this.rxStorageProvider.createDirectory(requestValues.parentFile, requestValues.name)
                .map(new Func1<RemoteFile, SingleResponseValue<RemoteFile>>() {
            @Override
            public SingleResponseValue<RemoteFile> call(RemoteFile remoteFile) {
                return new SingleResponseValue<>(remoteFile);
            }
        });
    }

    public static class RequestValues extends UseCase.RequestValues {
        public final RemoteFile parentFile;
        public final String name;

        public RequestValues(RemoteFile parentFile, String name) {
            this.parentFile = parentFile;
            this.name = name;
        }
    }
}
