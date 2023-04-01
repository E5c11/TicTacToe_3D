package com.esc.test.apps.board.io

import androidx.room.Database
import com.esc.test.apps.board.moves.data.MoveEntity
import com.esc.test.apps.board.games.data.Game
import androidx.room.RoomDatabase
import com.esc.test.apps.board.moves.io.MovesDao
import com.esc.test.apps.board.games.io.GamesDao

@Database(
    entities = [MoveEntity::class, Game::class],
    version = 2,
    exportSchema = true
)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun gameDao(): MovesDao
    abstract fun gamesDao(): GamesDao

    companion object {
        const val dbName = "player_history"
    }
}