package com.esc.test.apps.board.games.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.esc.test.apps.board.games.helpers.GameConstants.OPPONENT_PENDING
import com.esc.test.apps.board.games.helpers.GameConstants.START_PENDING
import com.esc.test.apps.board.games.helpers.GameConstants.WINNER_PENDING
import com.esc.test.apps.board.moves.data.Piece

@Entity(tableName = "games_table")
data class GameEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "timestamp") val id: Long = 0,
    @ColumnInfo(name = "winner") var winner: String = WINNER_PENDING,
    @ColumnInfo(name = "turn") var turn: Piece = Piece.CROSS,
    @ColumnInfo(name = "moves") var moves: Int = 0,
    @ColumnInfo(name = "opponent") var opponent: String = OPPONENT_PENDING,
    @ColumnInfo(name = "starter") var starter: String = START_PENDING,
    @ColumnInfo(name = "type") val type: Type = Type.LOCAL
)