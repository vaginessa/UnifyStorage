package org.cryse.unifystorage.explorer.application;

import android.app.Application;
import android.content.Context;

import rx.Scheduler;
import rx.schedulers.Schedulers;

public class UnifyStorageApplication extends Application {
    private Scheduler mDefaultSubscribeScheduler;

    public static UnifyStorageApplication get(Context context) {
        return (UnifyStorageApplication) context.getApplicationContext();
    }

    public Scheduler defaultSubscribeScheduler() {
        if (mDefaultSubscribeScheduler == null) {
            mDefaultSubscribeScheduler = Schedulers.io();
        }
        return mDefaultSubscribeScheduler;
    }

    //User to change scheduler from tests
    public void setDefaultSubscribeScheduler(Scheduler scheduler) {
        this.mDefaultSubscribeScheduler = scheduler;
    }
}
