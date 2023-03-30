package com.esc.test.apps.board.moves

import com.esc.test.apps.board.moves.data.Move
import com.esc.test.apps.common.utils.Resource
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.flow.Flow

interface MoveDataSource {
    suspend fun add(move: Move): Flow<Resource<Move>>
    fun getMove(): Flow<Resource<Move>>
    suspend fun getAllMoves(): Flow<Resource<List<Move>>>
}