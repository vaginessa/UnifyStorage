package org.cryse.unifystorage.explorer.utils;

import rx.Subscription;

public class RxSubscriptionUtils {
    public static void checkAndUnsubscribe(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }
}
