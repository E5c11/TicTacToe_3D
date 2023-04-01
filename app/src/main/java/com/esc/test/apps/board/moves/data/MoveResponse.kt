package com.esc.test.apps.board.moves.data

import com.google.gson.annotations.SerializedName

data class MoveResponse(
    @SerializedName("piece_played") val piecePlayed: String,
    @SerializedName("coordinates") val coordinates: String,
    @SerializedName("position") val position: String,
    @SerializedName("move_id") var id: String = "",
    @SerializedName("uid") var userId: String = "",
)