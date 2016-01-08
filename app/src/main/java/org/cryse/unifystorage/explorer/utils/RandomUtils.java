package org.cryse.unifystorage.explorer.utils;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomUtils {
    private static final AtomicInteger counter = new AtomicInteger();

    static {
        Random random = new Random(new Date().getTime());
        counter.set(random.nextInt());
    }

    public static int nextInt() {
        return counter.getAndIncrement();
    }
}
