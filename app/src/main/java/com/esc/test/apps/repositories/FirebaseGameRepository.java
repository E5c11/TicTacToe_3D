package com.esc.test.apps.repositories;

import android.app.Application;

import com.esc.test.apps.datastore.GameState;
import com.esc.test.apps.datastore.UserDetails;
import com.google.firebase.database.DatabaseReference;

import javax.inject.Singleton;

@Singleton
public class FirebaseGameRepository {

    private final DatabaseReference db;
    private final GameState gameState;
    private final UserDetails userDetails;
    private final Application app;

    public FirebaseGameRepository (GameState gameState, Application app,
                                   DatabaseReference db, UserDetails userDetails) {
        this.gameState = gameState;
        this.app = app;
        this.db = db;
        this.userDetails = userDetails;
    }
}
