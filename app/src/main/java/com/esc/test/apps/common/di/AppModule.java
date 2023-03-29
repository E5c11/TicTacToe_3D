package com.esc.test.apps.common.di;

import android.app.Application;

import androidx.room.Room;

import com.esc.test.apps.common.helpers.move.BotMoveGenerator;
import com.esc.test.apps.common.helpers.move.CheckMoveFactory;
import com.esc.test.apps.data.persistence.GamePreferences;
import com.esc.test.apps.data.persistence.UserPreferences;
import com.esc.test.apps.data.repositories.FbGameRepo;
import com.esc.test.apps.data.repositories.FbMoveRepo;
import com.esc.test.apps.data.repositories.FbUserRepo;
import com.esc.test.apps.data.repositories.implementations.remote.FirebaseGameRepository;
import com.esc.test.apps.data.repositories.implementations.remote.FirebaseMoveRepository;
import com.esc.test.apps.data.repositories.implementations.remote.FirebaseUserRepository;
import com.esc.test.apps.data.source.local.GameMovesDao;
import com.esc.test.apps.data.source.local.GamesDao;
import com.esc.test.apps.data.source.local.HistoryDatabase;
import com.esc.test.apps.common.network.ConnectionLiveData;
import com.esc.test.apps.domain.usecases.login.LoginUsecase;
import com.esc.test.apps.domain.usecases.moves.MovesUsecase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.checkerframework.checker.nullness.compatqual.PolyNullDecl;

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

    @Provides
    @Singleton
    public FbUserRepo provideFbUserRepo(FirebaseAuth firebaseAuth, DatabaseReference db,
                                        Application app, UserPreferences userPref) {
        return new FirebaseUserRepository(firebaseAuth, db, app, userPref);
    }

    @Provides
    @Singleton
    public FbMoveRepo provideFbMoveRepo(DatabaseReference db, UserPreferences userPref, GamePreferences gamePref) {
        return new FirebaseMoveRepository(db, userPref, gamePref);
    }

    @Provides
    @Singleton
    public FbGameRepo provideFbGameRepo(Application app, Random rand, GamePreferences gamePref,
                                        DatabaseReference db, FbMoveRepo fbMoveRepo, UserPreferences userPref) {
        return new FirebaseGameRepository(app, rand, gamePref, db, fbMoveRepo, userPref);
    }

    @Provides
    public LoginUsecase provideLoginUsecase(UserPreferences userPref, FbUserRepo fbUserRepo) {
        return new LoginUsecase(userPref, fbUserRepo);
    }

    @Provides
    public MovesUsecase provideMovesUsecase(CheckMoveFactory checkMoveFactory,
                                            BotMoveGenerator botMoveGenerator, GamePreferences gamePref) {
        return new MovesUsecase(checkMoveFactory, botMoveGenerator, gamePref);
    }
}
