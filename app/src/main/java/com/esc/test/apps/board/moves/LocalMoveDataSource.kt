package com.esc.test.apps.board.moves

import com.esc.test.apps.board.moves.data.Move
import com.esc.test.apps.board.moves.data.toMove
import com.esc.test.apps.board.moves.data.toMoveEntity
import com.esc.test.apps.board.moves.data.toMoveList
import com.esc.test.apps.board.moves.io.GameMovesDao
import com.esc.test.apps.common.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalMoveDataSource @Inject constructor(
    private val dao: GameMovesDao
): MoveDataSource {

    override suspend fun add(move: Move): Flow<Resource<Move>> = flow {
        try {
            dao.insert(move.toMoveEntity())
            emit(Resource.success(move))
        } catch (e: Exception) {
            emit(Resource.error(e))
        }
    }

    override fun getMove(): Flow<Resource<Move>> {
        return try {
            dao.getMove().map { Resource.success(it.toMove()) }
        } catch (e: Exception) {
            flow { emit(Resource.error(e)) }
        }
    }

    override suspend fun getAllMoves(): Flow<Resource<List<Move>>> = flow {
        try {
            val moves = dao.getAllMoves()
            emit(Resource.success(moves.toMoveList()))
        } catch (e: Exception) {
            emit(Resource.error(e))
        }
    }
}