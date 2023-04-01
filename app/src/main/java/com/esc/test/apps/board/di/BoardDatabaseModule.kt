package com.esc.test.apps.board.di

import android.app.Application
import androidx.room.Room
import com.esc.test.apps.board.io.HistoryDatabase
import com.esc.test.apps.board.moves.LocalMoveDataSource
import com.esc.test.apps.board.moves.MoveDataSource
import com.esc.test.apps.board.moves.RemoteMoveDataSource
import com.esc.test.apps.board.moves.io.MovesDao
import com.esc.test.apps.data.persistence.GamePreferences
import com.esc.test.apps.data.persistence.UserPreferences
import com.esc.test.apps.data.source.local.GamesDao
import com.google.firebase.database.DatabaseReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Remote

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Local


@Module
@InstallIn(SingletonComponent::class)
object BoardDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application?): HistoryDatabase {
        return Room.databaseBuilder(app!!, HistoryDatabase::class.java, HistoryDatabase.dbName)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideGameMovesDao(db: HistoryDatabase): MovesDao = db.gameDao()


    @Provides
    @Singleton
    fun provideGamesDao(db: HistoryDatabase): GamesDao = db.gamesDao()

    @Provides
    @Singleton
    @Local
    fun providesLocalMoveDataSource(moveDao: MovesDao): MoveDataSource = LocalMoveDataSource(moveDao)

    @Provides
    @Singleton
    @Remote
    fun providesRemoteMoveDataSource(
        dbRef: DatabaseReference,
        userPref: UserPreferences,
        gamePref: GamePreferences
    ): MoveDataSource = RemoteMoveDataSource(dbRef, userPref, gamePref)

}