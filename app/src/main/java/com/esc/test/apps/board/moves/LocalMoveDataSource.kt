package com.esc.test.apps.board.moves

import com.esc.test.apps.board.moves.data.*
import com.esc.test.apps.board.moves.io.MovesDao
import com.esc.test.apps.common.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalMoveDataSource @Inject constructor(
    private val dao: MovesDao
): MoveDataSource {

    override suspend fun add(move: Move): Flow<Resource<Move>> = flow {
        try {
            val id = dao.insert(move.toMoveEntity())
            emit(Resource.success(move.copy(id = id.toString())))
        } catch (e: Exception) {
            emit(Resource.error(e))
        }
    }

    override suspend fun addAll(vararg move: Move): Flow<Resource<Int>> = flow {
        try {
            val idList = dao.insertMoves(*move.toMoveEntityArray())
            emit(Resource.success(idList.size - 1))
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