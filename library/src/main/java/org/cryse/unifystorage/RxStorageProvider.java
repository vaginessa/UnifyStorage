package org.cryse.unifystorage;

import org.cryse.unifystorage.utils.DirectoryPair;

import java.io.InputStream;

import java.util.List;


import rx.Observable;
import rx.Subscriber;


public class RxStorageProvider<RF extends RemoteFile, SP extends StorageProvider<RF>> {
    private SP mStorageProvider;

    public RxStorageProvider(SP storageProvider) {
        this.mStorageProvider = storageProvider;
    }

    public Observable<RF> getRootDirectory() {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.getRootDirectory());
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }

    public Observable<DirectoryPair<RF, List<RF>>> list(final RF parent) {
        return Observable.create(new Observable.OnSubscribe<DirectoryPair<RF, List<RF>>>() {
            @Override
            public void call(Subscriber<? super DirectoryPair<RF, List<RF>>> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.list(parent));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }

    public Observable<DirectoryPair<RF, List<RF>>> list() {
        return Observable.create(new Observable.OnSubscribe<DirectoryPair<RF, List<RF>>>() {
            @Override
            public void call(Subscriber<? super DirectoryPair<RF, List<RF>>> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.list());
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }

    public Observable<RF> createDirectory(final RF parent, final String name) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createDirectory(parent, name));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }

    public Observable<RF> createDirectory(final String name) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createDirectory(name));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }

    public Observable<RF> createFile(final RF parent, final String name, final InputStream input, final ConflictBehavior behavior) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createFile(parent, name, input, behavior));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RF> createFile(final RF parent, final String name, final InputStream input) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createFile(parent, name, input));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RF> createFile(final String name, final InputStream input) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createFile(name, input));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RF> createFile(final RF parent, final String name, final LocalFile file, final ConflictBehavior behavior) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createFile(parent, name, file, behavior));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RF> createFile(final RF parent, final String name, final LocalFile file) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createFile(parent, name, file));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RF> createFile(final String name, final LocalFile file) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createFile(name, file));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<Boolean> exists(final RF parent, final String name) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.exists(parent, name));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<Boolean> exists(final String name) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.exists(name));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RF> getFile(final RF parent, final String name) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.getFile(parent, name));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RF> getFile(final String name) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.getFile(name));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RF> getFileById(final String id) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.getFileById(id));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RF> updateFile(final RF remote, final InputStream input, final FileUpdater updater) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.updateFile(remote, input, updater));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RF> updateFile(final RF remote, final InputStream input) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.updateFile(remote, input));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RF> updateFile(final RF remote, final LocalFile local, final FileUpdater updater) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.updateFile(remote, local, updater));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RF> updateFile(final RF remote, final LocalFile local) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.updateFile(remote, local));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<Boolean> deleteFile(final RF file) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.deleteFile(file));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RF> getFileDetail(final RF file) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.getFileDetail(file));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });

    }


    public Observable<RF> getFilePermission(final RF file) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.getFilePermission(file));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });

    }


    public Observable<RF> updateFilePermission(final RF file) {
        return Observable.create(new Observable.OnSubscribe<RF>() {
            @Override
            public void call(Subscriber<? super RF> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.updateFilePermission(file));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });

    }


    public Observable<StorageUserInfo> getUserInfo() {
        return Observable.create(new Observable.OnSubscribe<StorageUserInfo>() {
            @Override
            public void call(Subscriber<? super StorageUserInfo> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.getUserInfo());
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });

    }


    public Observable<StorageUserInfo> getUserInfo(final boolean forceRefresh) {
        return Observable.create(new Observable.OnSubscribe<StorageUserInfo>() {
            @Override
            public void call(Subscriber<? super StorageUserInfo> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.getUserInfo(forceRefresh));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });

    }

    SP getStorageProvider() {
        return mStorageProvider;
    }

    HashAlgorithm getHashAlgorithm() {
        return mStorageProvider.getHashAlgorithm();
    }

}
