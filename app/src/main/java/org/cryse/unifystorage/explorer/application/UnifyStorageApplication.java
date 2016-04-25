package org.cryse.unifystorage.explorer.application;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

import org.cryse.unifystorage.explorer.data.UnifyStorageDatabase;
import org.cryse.unifystorage.explorer.utils.copy.CopyManager;
import org.cryse.utils.preference.Prefs;

import rx.Scheduler;
import rx.schedulers.Schedulers;

public class UnifyStorageApplication extends Application {
    private Scheduler mDefaultSubscribeScheduler;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        Prefs.with(this).useDefault().init();
        UnifyStorageDatabase.init(this);
        StorageProviderManager.init(this);
        CopyManager.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        StorageProviderManager.destroy();
        UnifyStorageDatabase.destroy();
    }

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
