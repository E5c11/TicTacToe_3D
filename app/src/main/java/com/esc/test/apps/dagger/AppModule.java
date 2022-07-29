package com.esc.test.apps.dagger;

import android.app.Application;

import androidx.room.Room;

import com.esc.test.apps.room.GameMovesDao;
import com.esc.test.apps.room.GamesDao;
import com.esc.test.apps.room.HistoryDatabase;
import com.esc.test.apps.network.ConnectionLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public HistoryDatabase provideDatabase(Application app) {
        return Room.databaseBuilder(app, HistoryDatabase.class, HistoryDatabase.dbName)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    public GameMovesDao provideGameMovesDao(HistoryDatabase db) {
        return db.gameDao();
    }

    @Provides
    @Singleton
    public GamesDao provideGamesDao(HistoryDatabase db) {
        return db.gamesDao();
    }

    @Provides
    @Singleton
    public DatabaseReference provideDataBaseRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    @Provides
    @Singleton
    public FirebaseAuth provideFireAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    @Singleton
    public ConnectionLiveData provideNetworkReport(Application app) {
        return new ConnectionLiveData(app);
    }

    @Provides
    @Singleton
    public Random provideRandomClass() { return new Random(); }
}
