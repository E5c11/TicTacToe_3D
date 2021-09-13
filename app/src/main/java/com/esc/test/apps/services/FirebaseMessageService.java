package com.esc.test.apps.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.esc.test.apps.repositories.FirebaseUserRepository;
import com.google.firebase.messaging.FirebaseMessagingService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FirebaseMessageService extends FirebaseMessagingService {

    @Inject FirebaseUserRepository fbUserRepo;

    @Override
    public void onNewToken(@NonNull String s) {
        fbUserRepo.setToken(s);
        Log.d("myT", "onNewToken: " + s);
    }
}
