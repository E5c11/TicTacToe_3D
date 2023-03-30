package com.esc.test.apps.board.moves.data

fun MoveEntity.toMove() = Move(
    coordinates = this.coordinates,
    position = this.position,
    piecePlayed = this.piecePlayed
)

fun Move.toMoveEntity() = MoveEntity(
    id = "",
    coordinates = this.coordinates,
    position = this.position,
    piecePlayed = this.piecePlayed
)

fun MoveResponse.toMove() = Move(
    coordinates = this.coordinates,
    position = this.position,
    piecePlayed = this.piecePlayed
)

fun List<MoveEntity>.toMoveList(): List<Move> {
    val list = mutableListOf<Move>()
    this.forEach { move ->
        list.add(move.toMove())
    }
    return list.toList()
}

fun emptyMove() = Move(coordinates = "", position = "", piecePlayed = "")