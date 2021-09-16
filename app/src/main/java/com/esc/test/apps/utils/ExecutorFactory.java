package com.esc.test.apps.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorFactory {

    private static ExecutorService executor;

    public static ExecutorService getSingleExecutor() {
        return Executors.newSingleThreadExecutor();
    }
    public static ExecutorService getFixedSizeExecutor() {
        return Executors.newFixedThreadPool(3);
    }
}
