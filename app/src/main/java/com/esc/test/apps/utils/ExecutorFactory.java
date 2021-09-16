package com.esc.test.apps.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorFactory {

    private static final ExecutorService singleExecutor = Executors.newSingleThreadExecutor();
    private static final ExecutorService fixedSizeExecutor = Executors.newFixedThreadPool(3);

    public static ExecutorService getSingleExecutor() {
        return singleExecutor;
    }
    public static ExecutorService getFixedSizeExecutor() {
        return fixedSizeExecutor;
    }

    public static void killSingleExecutor() {
        singleExecutor.shutdownNow();
    }

    public static void killFixedSizeExecutor() {
        fixedSizeExecutor.shutdownNow();
    }

}
