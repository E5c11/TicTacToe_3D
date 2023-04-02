package com.esc.test.apps.board.moves

import com.esc.test.apps.board.di.Local
import com.esc.test.apps.board.di.Remote
import com.esc.test.apps.board.moves.data.Move
import com.esc.test.apps.common.utils.Constants.FETCHING_DATA
import com.esc.test.apps.common.utils.Constants.SAVING
import com.esc.test.apps.common.utils.Resource
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class MoveRepository @Inject constructor(
    @Remote private val remote: MoveDataSource,
    @Local private val local: MoveDataSource
) {

    fun insert(move: Move) = flow {
        emit(Resource.loading(SAVING))
        try {
            val moveWithId = local.add(move).first().data
            remote.add(moveWithId!!)
            emit(Resource.success(moveWithId))
        } catch (e: Exception) {
            emit(Resource.error(e))
        }
    }

    private fun insertAll(vararg move: Move) = flow {
        emit(Resource.loading(SAVING))
        try {
            val lastPlayMoveId = local.addAll(*move).first().data
            emit(Resource.success(lastPlayMoveId))
        } catch (e: Exception) {
            emit(Resource.error(e))
        }
    }

    fun observeLatest() = flow {
        try {
            local.getMove().collect {
                it.apply {
                    emit(Resource(status, data, error, loading))
                }
            }
        } catch (e: Exception) {
            emit(Resource.error(e))
        }
    }

    suspend fun fetchRemote() = remote.getMove().collect { resource ->
        resource.data?.let { insert(it) }
    }

    suspend fun getAllRemoteMoves() = flow {
        emit(Resource.loading(FETCHING_DATA))
        try {
            val moves: Resource<List<Move>> = remote.getAllMoves().first()
            moves.data?.let {
                insertAll(*it.toTypedArray())
            }
            emit(Resource.success(moves.data))
        } catch (e: Exception) {
            emit(Resource.error(e))
        }
    }

    suspend fun getAll(flow: MutableSharedFlow<Resource<List<Move>>>) {
        flow.emit(Resource.loading(FETCHING_DATA))
        try {
            local.getAllMoves().collect {
                it.apply {
                    flow.emit(Resource(status, data, error, loading))
                }
            }
        } catch (e: Exception) {
            flow.emit(Resource.error(e))
        }
    }

}