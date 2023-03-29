package com.esc.test.apps.board.moves.io

import com.esc.test.apps.common.utils.Resource
import com.esc.test.apps.data.models.pojos.MoveInfo
import kotlinx.coroutines.flow.Flow

interface MoveDataSource {
    fun add(move: MoveInfo): Flow<Resource<MoveInfo>>
    fun getMove(): Flow<Resource<MoveInfo>>
    fun getAllMoves(): Flow<Resource<List<MoveInfo>>>
}