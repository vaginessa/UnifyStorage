package org.cryse.unifystorage.explorer.base;

import org.cryse.unifystorage.explorer.executor.PostExecutionThread;
import org.cryse.unifystorage.explorer.executor.ThreadExecutor;

public interface BasePresenter {

    void start();

    void destroy();

    abstract class Builder<P extends BasePresenter, V extends BaseView<P>> {
        protected ThreadExecutor threadExecutor;
        protected PostExecutionThread postExecutionThread;
        protected V view;

        public Builder() {

        }

        public Builder<P, V> view(V view) {
            this.view = view;
            return this;
        }

        public abstract Builder<P, V> threadExecutor(ThreadExecutor threadExecutor);

        public abstract Builder<P, V> postExecutionThread(PostExecutionThread postExecutionThread);

        public abstract P build();
    }
}
