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

enum class State { NONE, WAITING, CONFIRMED, WINNER }

enum class Piece { CROSS, CIRCLE }

enum class Colour { RED, BLACK }