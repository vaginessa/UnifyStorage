package org.cryse.unifystorage;

import android.support.v4.util.Pair;

import org.cryse.unifystorage.utils.OperationResult;
import org.cryse.unifystorage.utils.DirectoryInfo;
import org.cryse.unifystorage.utils.ProgressCallback;

import java.io.InputStream;


import rx.Observable;
import rx.Subscriber;


public class RxStorageProvider {
    private StorageProvider mStorageProvider;

    public RxStorageProvider(StorageProvider storageProvider) {
        this.mStorageProvider = storageProvider;
    }

    public String getStorageProviderName() {
        return mStorageProvider.getStorageProviderName();
    }

    public Observable<RemoteFile> getRootDirectory() {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.getRootDirectory());
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }

    public Observable<DirectoryInfo> list(final DirectoryInfo directoryInfo) {
        return Observable.create(new Observable.OnSubscribe<DirectoryInfo>() {
            @Override
            public void call(Subscriber<? super DirectoryInfo> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.list(directoryInfo));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }

    public Observable<DirectoryInfo> list() {
        return Observable.create(new Observable.OnSubscribe<DirectoryInfo>() {
            @Override
            public void call(Subscriber<? super DirectoryInfo> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.list());
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }

    public Observable<RemoteFile> createDirectory(final RemoteFile parent, final String name) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createDirectory(parent, name));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }

    public Observable<RemoteFile> createDirectory(final String name) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createDirectory(name));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }

    public Observable<RemoteFile> createFile(final RemoteFile parent, final String name, final InputStream input, final ConflictBehavior behavior) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createFile(parent, name, input, behavior));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RemoteFile> createFile(final RemoteFile parent, final String name, final InputStream input) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createFile(parent, name, input));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RemoteFile> createFile(final String name, final InputStream input) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createFile(name, input));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RemoteFile> createFile(final RemoteFile parent, final String name, final LocalFile file, final ConflictBehavior behavior) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createFile(parent, name, file, behavior));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RemoteFile> createFile(final RemoteFile parent, final String name, final LocalFile file) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createFile(parent, name, file));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RemoteFile> createFile(final String name, final LocalFile file) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.createFile(name, file));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<Boolean> exists(final RemoteFile parent, final String name) {
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


    public Observable<RemoteFile> getFile(final RemoteFile parent, final String name) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.getFile(parent, name));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RemoteFile> getFile(final String name) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.getFile(name));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RemoteFile> getFileById(final String id) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.getFileById(id));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RemoteFile> updateFile(final RemoteFile remote, final InputStream input, final FileUpdater updater) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.updateFile(remote, input, updater));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RemoteFile> updateFile(final RemoteFile remote, final InputStream input) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.updateFile(remote, input));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RemoteFile> updateFile(final RemoteFile remote, final LocalFile local, final FileUpdater updater) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.updateFile(remote, local, updater));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RemoteFile> updateFile(final RemoteFile remote, final LocalFile local) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.updateFile(remote, local));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<OperationResult> deleteFile(final RemoteFile...files) {
        return Observable.create(new Observable.OnSubscribe<OperationResult>() {
            @Override
            public void call(Subscriber<? super OperationResult> subscriber) {
                try {
                    for(RemoteFile file : files) {
                        subscriber.onNext(mStorageProvider.deleteFile(file));
                    }
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<OperationResult> copyFiles(final RemoteFile target, final RemoteFile...files) {
        return Observable.create(new Observable.OnSubscribe<OperationResult>() {
            @Override
            public void call(final Subscriber<? super OperationResult> subscriber) {
                try {
                    long totalSize = 0;
                    for (RemoteFile file : files) {
                        totalSize += file.size();
                    }
                    final int totalFileCount = files.length;
                    final int[] currentFileCount = new int[]{0};
                    mStorageProvider.copyFile(target, new ProgressCallback() {
                        @Override
                        public void onSuccess() {
                            currentFileCount[0]++;
                            // subscriber.onNext(Pair.create(file, true));
                            if (currentFileCount[0] == totalFileCount)
                                subscriber.onCompleted();
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            currentFileCount[0]++;
                            // subscriber.onNext(Pair.create(file, false));
                            if (currentFileCount[0] == totalFileCount)
                                subscriber.onCompleted();
                        }

                        @Override
                        public void onProgress(long current, long max) {

                        }
                    }, files);
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });
    }


    public Observable<RemoteFile> getFileDetail(final RemoteFile file) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.getFileDetail(file));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });

    }


    public Observable<RemoteFile> getFilePermission(final RemoteFile file) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
                try {
                    subscriber.onNext(mStorageProvider.getFilePermission(file));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        });

    }


    public Observable<RemoteFile> updateFilePermission(final RemoteFile file) {
        return Observable.create(new Observable.OnSubscribe<RemoteFile>() {
            @Override
            public void call(Subscriber<? super RemoteFile> subscriber) {
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

    public StorageProvider getStorageProvider() {
        return mStorageProvider;
    }

    HashAlgorithm getHashAlgorithm() {
        return mStorageProvider.getHashAlgorithm();
    }

}
