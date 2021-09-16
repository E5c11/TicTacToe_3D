package com.esc.test.apps;

import android.app.Application;

import com.esc.test.apps.utils.ExecutorFactory;

import java.util.concurrent.ExecutorService;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class AppClass extends Application {
    @Override
    public void onTerminate() {
        super.onTerminate();
        ExecutorFactory.killFixedSizeExecutor();
        ExecutorFactory.killSingleExecutor();
    }
}
