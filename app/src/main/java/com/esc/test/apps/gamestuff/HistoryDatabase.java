package com.esc.test.apps.gamestuff;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.esc.test.apps.entities.Game;
import com.esc.test.apps.entities.Move;

@Database(entities = {Move.class, Game.class}, version = 2)
public abstract class HistoryDatabase extends RoomDatabase {

    public abstract GameMovesDao gameDao();

    public abstract GamesDao gamesDao();

    public static final String dbName = "player_history";

}
