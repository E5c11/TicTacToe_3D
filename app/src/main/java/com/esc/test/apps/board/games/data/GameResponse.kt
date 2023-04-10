package com.esc.test.apps.board.games.data

import com.esc.test.apps.board.games.helpers.GameConstants
import com.esc.test.apps.board.moves.data.Piece
import com.google.gson.annotations.SerializedName

data class GameResponse(
    @SerializedName("winner") var winner: String = GameConstants.WINNER_PENDING,
    @SerializedName("turn") var turn: Piece,
    @SerializedName("moves") var moves: Int = 0,
    @SerializedName("opponent") var opponent: String,
    @SerializedName("starter") var starter: String
)
