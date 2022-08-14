package com.esc.test.apps.network.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.esc.test.apps.data.datastore.UserDetail;
import com.esc.test.apps.data.datastore.UserPreferences;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FirebaseMessageService extends FirebaseMessagingService {

    @Inject UserDetail user;
    @Inject UserPreferences userPref;

    @Override
    public void onNewToken(@NonNull String s) {
        Log.d("[Firebase Token]", "onNewToken: " + s);
//        user.setToken(s);
        userPref.updateTokenJava(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }
}
