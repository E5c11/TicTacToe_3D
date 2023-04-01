package com.esc.test.apps.board.moves.data

fun MoveEntity.toMove() = Move(
    coordinates = this.coordinates,
    position = this.position,
    piecePlayed = this.piecePlayed,
    id = this.id
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

fun Move.toRemoteMove(userId: String) = MoveResponse(
    coordinates = this.coordinates,
    position = this.position,
    piecePlayed = this.piecePlayed,
    userId = userId
)

fun Array<out Move>.toMoveEntityArray(): Array<out MoveEntity> =
    this.map { it.toMoveEntity() }.toTypedArray()

fun List<MoveEntity>.toMoveList(): List<Move> {
    val list = mutableListOf<Move>()
    this.forEach { move ->
        list.add(move.toMove())
    }
    return list.toList()
}

fun emptyMove() = Move(coordinates = "", position = "", piecePlayed = "")