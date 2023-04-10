package com.esc.test.apps.board.moves

import com.esc.test.apps.board.moves.data.Move
import com.esc.test.apps.board.moves.helpers.MoveFlow
import com.esc.test.apps.board.moves.helpers.MovesFlow
import com.esc.test.apps.common.helpers.RoomFlow
import com.esc.test.apps.common.utils.Resource
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.flow.Flow

interface MoveDataSource {
    suspend fun add(move: Move): MoveFlow
    suspend fun addAll(vararg move: Move): RoomFlow
    fun getMove(): MoveFlow
    suspend fun getAllMoves(): MovesFlow
}