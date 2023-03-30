package com.esc.test.apps.domain.usecases.moves

import android.util.Log
import com.esc.test.apps.common.helpers.move.BotMoveGenerator
import com.esc.test.apps.common.helpers.move.CheckMoveFactory
import com.esc.test.apps.board.moves.data.MoveEntity
import com.esc.test.apps.data.persistence.GamePreferences
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class MovesUsecase @Inject constructor(private val checkMoveFactory: CheckMoveFactory,
                                       private val botMoveGenerator: BotMoveGenerator,
                                       private val gamePref: GamePreferences
) {
    private lateinit var d: Disposable

    fun invoke(coord: String, userPiece: String, count: Int, online: Boolean) =
        checkMoveFactory.createMoves(coord, userPiece, count.toString(), online)

    fun invoke(piece: String, moveCount: Int) = botMoveGenerator.setPiece(piece, moveCount)

    fun invoke() = botMoveGenerator.newGame()

    fun invoke(moveEntity: MoveEntity) {
        d = gamePref.gamePreference.subscribeOn(Schedulers.io()).subscribe( { pref ->
            if (pref.winner.isEmpty()) botMoveGenerator.eliminateLines(moveEntity)
        }, {
            Log.d("myT", "invoke: no moves")
            it.stackTrace
        }, { })
    }

}