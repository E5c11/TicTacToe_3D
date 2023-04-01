package com.esc.test.apps.board.moves.data

data class Move(
    val coordinates: String,
    val position: String,
    val piecePlayed: String,
    val id: String = "",
    val uid: String = ""
)
