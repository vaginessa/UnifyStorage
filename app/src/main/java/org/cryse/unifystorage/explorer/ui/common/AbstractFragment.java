package org.cryse.unifystorage.explorer.ui.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.cryse.unifystorage.explorer.event.AbstractEvent;
import org.cryse.unifystorage.explorer.event.RxEventBus;
import org.cryse.unifystorage.explorer.utils.ResourceUtils;
import org.cryse.unifystorage.explorer.utils.RxSubscriptionUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public abstract class AbstractFragment extends Fragment {
    protected RxEventBus mEventBus = RxEventBus.instance();
    private Subscription mEventBusSubscription;
    protected int mPrimaryColor;
    protected int mToolbarContentColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventBusSubscription = mEventBus.toObservable()
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<AbstractEvent>() {
                    @Override
                    public void call(AbstractEvent abstractEvent) {
                        onEvent(abstractEvent);
                    }
                });

        mPrimaryColor = ResourceUtils.primaryColor(getContext());
        mToolbarContentColor = ResourceUtils.toolbarTextColor(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxSubscriptionUtils.checkAndUnsubscribe(mEventBusSubscription);
    }

    protected void onEvent(AbstractEvent event) {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Apply theming to the Fragment view
    }

    public AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }
}
