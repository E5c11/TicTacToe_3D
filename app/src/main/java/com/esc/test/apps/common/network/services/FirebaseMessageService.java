package com.esc.test.apps.common.network.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.esc.test.apps.data.persistence.UserPreferences;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FirebaseMessageService extends FirebaseMessagingService {

    @Inject UserPreferences userPref;

    @Override
    public void onNewToken(@NonNull String s) {
        Log.d("[Firebase Token]", "onNewToken: " + s);
        userPref.updateTokenJava(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }
}
