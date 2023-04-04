package com.esc.test.apps.board.moves.helpers

import com.esc.test.apps.R
import com.esc.test.apps.board.moves.data.Colour
import com.esc.test.apps.board.moves.data.Layer
import com.esc.test.apps.board.moves.data.Move
import com.esc.test.apps.board.moves.data.Piece
import com.esc.test.apps.board.moves.helpers.MoveConstants.NO_CUBES_PER_LAYER
import com.esc.test.apps.board.moves.helpers.MoveConstants.NO_OF_LAYERS

fun Move.getTurnPiece(): Int = when (this.piecePlayed) {
    Piece.CROSS -> {
        if (Colour.RED == this.color) R.drawable.red_cross
        else R.drawable.black_cross
    }
    else -> {
        if (Colour.RED == this.color) R.drawable.red_circle
        else R.drawable.black_circle
    }
}

fun Layer.createMovePlaceHolders(): List<Move> {
    val moves = mutableListOf<Move>()
    var x = 0; var y = 0
    for (i in 0..NO_CUBES_PER_LAYER) {
        val cubeCoordinates = "$x$y$this"
        val cubePos = (x * NO_OF_LAYERS + y + this * NO_CUBES_PER_LAYER).toString()
        if (y <= 2) y++
        else {
            x++
            y = 0
        }
        moves.add(Move(cubeCoordinates, cubePos))
    }
    return moves
}