package com.esc.test.apps.other;


import android.app.Application;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class ResourceProvider {

    private final Application app;

    @Inject
    public ResourceProvider(Application app) {
        this.app = app;
    }

    public String getString(int resId) {
        return app.getString(resId);
    }

    public String getString(int resId, String value) {
        return app.getString(resId, value);
    }
}
