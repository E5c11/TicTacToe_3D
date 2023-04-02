package com.esc.test.apps.board.moves.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_moves_table")
data class MoveEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "coordinates") var coordinates: String,
    @ColumnInfo(name = "position") var position: String,
    @ColumnInfo(name = "piece_played") var piecePlayed: String,
    @ColumnInfo(name = "winner") var winner: String
)