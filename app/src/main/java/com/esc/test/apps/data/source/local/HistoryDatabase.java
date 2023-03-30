package com.esc.test.apps.data.source.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.esc.test.apps.board.moves.data.MoveEntity;
import com.esc.test.apps.board.moves.io.GameMovesDao;
import com.esc.test.apps.data.models.entities.Game;

@Database(entities = {MoveEntity.class, Game.class}, version = 2)
public abstract class HistoryDatabase extends RoomDatabase {

    public abstract GameMovesDao gameDao();

    public abstract GamesDao gamesDao();

    public static final String dbName = "player_history";

}
