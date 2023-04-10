package com.esc.test.apps.board.moves.data

data class Move(
    var coordinates: String,
    var position: String,
    var piecePlayed: Piece? = null,
    var color: Colour? = null,
    var id: String = "",
    var uid: String = "",
    var state: State = State.NONE
)

enum class State(val value: Int) { NONE(0), WAITING(1), CONFIRMED(2), WINNER(3) }

enum class Piece(val value: Int) { CROSS(1), CIRCLE(0) }

enum class Colour(val value: Int) { RED(0), BLACK(1) }