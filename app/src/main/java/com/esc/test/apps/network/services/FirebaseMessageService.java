package com.esc.test.apps.network.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.esc.test.apps.data.datastore.UserDetails;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FirebaseMessageService extends FirebaseMessagingService {

    @Inject UserDetails user;

    @Override
    public void onNewToken(@NonNull String s) {
        Log.d("myT", "onNewToken: " + s);
        user.setToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }
}
