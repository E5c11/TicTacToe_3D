package com.esc.test.apps.board.moves.io

import androidx.room.Dao
import com.esc.test.apps.board.moves.data.MoveEntity
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovesDao {
    @Insert
    suspend fun insert(moveEntity: MoveEntity): Long

    @Insert
    suspend fun insertMoves(vararg moveEntities: MoveEntity): Array<Long>

    @Query("DELETE FROM game_moves_table")
    suspend fun deleteGame()

    @Query("SELECT position FROM game_moves_table WHERE position = :position")
    fun getPosition(position: String): Flow<String>

    @Query("SELECT piece_played FROM game_moves_table WHERE position = :position")
    suspend fun getPiecePlayed(position: String): String

    @Query("SELECT * FROM game_moves_table ORDER BY position ASC")
    suspend fun getAllMoves(): List<MoveEntity>

    @Query("SELECT * FROM game_moves_table ORDER BY id DESC LIMIT 1")
    fun getMove(): Flow<MoveEntity>

    @Query("SELECT piece_played FROM game_moves_table WHERE id = 1")
    fun getFirstMove(): Flow<String>
}