package com.esc.test.apps.board.moves

import com.esc.test.apps.board.moves.io.exception.MoveNotRetrievedException
import com.esc.test.apps.common.utils.Constants.FETCHING_DATA
import com.esc.test.apps.common.utils.Constants.SAVING
import com.esc.test.apps.common.utils.DatabaseConstants.*
import com.esc.test.apps.common.utils.Resource
import com.esc.test.apps.common.utils.awaitsSingle
import com.esc.test.apps.common.utils.observeValue
import com.esc.test.apps.board.moves.data.MoveResponse
import com.esc.test.apps.data.persistence.GamePreferences
import com.esc.test.apps.data.persistence.UserPreferences
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

const val SET_REGEX = "_"

class RemoteMoveDataSource @Inject constructor(
    private val dbRef: DatabaseReference,
    private val userPrefs: UserPreferences,
    private val gamePrefs: GamePreferences
): MoveDataSource {

    override fun add(move: MoveResponse): Flow<Resource<MoveResponse>> = flow {
        emit(Resource.loading(SAVING))
        try {
            val uid = userPrefs.userPref.first().uid
            val game = gamePrefs.gamePref.first()
            move.uid = uid
            dbRef.child(GAMES).child(game.setId).child(game.id).child(MOVES).child(move.moveID)
                .setValue(move)
                .await()
            emit(Resource.success(move))
        } catch (e: Exception) {
            emit(Resource.error(e))
        }
    }

    override fun getMove(): Flow<Resource<MoveResponse>> = callbackFlow {
        trySend(Resource.loading(FETCHING_DATA))
        try {
            val uid = userPrefs.userPref.first().uid
            val game = gamePrefs.gamePref.first()
            dbRef.child(USERS).child(uid).child(FRIENDS)
                .child(getFriendUid(game.setId, uid))
                .child(MOVE)
                .observeValue().map { snapshot ->
                    val move = snapshot?.getValue(MoveResponse::class.java)
                    if (move != null) trySend(Resource.success(move))
                    else trySend(Resource.error(MoveNotRetrievedException()))
                }
        } catch (e: Exception) {
            trySend(Resource.error(e))
        }
    }

    override fun getAllMoves(): Flow<Resource<List<MoveResponse>>> = callbackFlow {
        trySend(Resource.loading(FETCHING_DATA))
        try {
            val list = mutableListOf<MoveResponse>()
            val game = gamePrefs.gamePref.first()
            val moves = dbRef.child(GAMES).child(game.setId).child(game.id).child(MOVES)
                .awaitsSingle()?.children
            moves?.forEach { snapshot ->
                val move = snapshot.getValue(MoveResponse::class.java)
                move?.let { list.add(move) }
            }
            trySend(Resource.success(list))
        } catch (e: Exception) {
            trySend(Resource.error(e))
        }
    }

    private fun getFriendUid(gameSetId: String, uid: String): String {
        val uids = gameSetId.split(SET_REGEX).toTypedArray()
        return if (uids[0] == uid) uids[1] else uids[0]
    }
}