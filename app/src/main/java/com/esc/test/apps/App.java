package com.esc.test.apps;

import android.app.Application;
import android.content.res.Resources;

import com.esc.test.apps.common.utils.ExecutorFactory;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class App extends Application {

    public static Resources res;

    @Override
    public void onCreate() {
        super.onCreate();
        res = getResources();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ExecutorFactory.killFixedSizeExecutor();
        ExecutorFactory.killSingleExecutor();
    }
}
