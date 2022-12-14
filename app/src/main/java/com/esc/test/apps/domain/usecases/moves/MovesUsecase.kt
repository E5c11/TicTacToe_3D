package com.esc.test.apps.domain.usecases.moves

import com.esc.test.apps.common.helpers.move.BotMoveGenerator
import com.esc.test.apps.common.helpers.move.CheckMoveFactory
import javax.inject.Inject

class MovesUsecase @Inject constructor(private val checkMoveFactory: CheckMoveFactory, private val botMoveGenerator: BotMoveGenerator) {

    fun invoke(coord: String, userPiece: String, count: Int, online: Boolean) =
        checkMoveFactory.createMoves(coord, userPiece, count.toString(), online)

    fun invoke(piece: String, moveCount: Int) = botMoveGenerator.setPiece(piece, moveCount)

    fun invoke() = botMoveGenerator.newGame()

}