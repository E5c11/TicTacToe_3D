package com.esc.test.apps.board.games

import com.esc.test.apps.board.games.data.Game
import com.esc.test.apps.board.games.helpers.GameFlow
import com.esc.test.apps.board.games.helpers.GamesFlow
import com.esc.test.apps.board.moves.data.Move
import com.esc.test.apps.board.moves.data.MoveResponse
import com.esc.test.apps.board.moves.data.toMove
import com.esc.test.apps.board.moves.io.exception.MoveNotRetrievedException
import com.esc.test.apps.common.helpers.RoomFlow
import com.esc.test.apps.common.utils.Constants
import com.esc.test.apps.common.utils.DatabaseConstants
import com.esc.test.apps.common.utils.DatabaseConstants.FRIENDS
import com.esc.test.apps.common.utils.DatabaseConstants.GAMES
import com.esc.test.apps.common.utils.Resource
import com.esc.test.apps.common.utils.observeValue
import com.esc.test.apps.data.persistence.UserPreferences
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RemoteGameDataSource @Inject constructor(
    private val userPrefs: UserPreferences,
    private val dbRef: DatabaseReference
): GameDataSource {
    override suspend fun add(game: Game): GameFlow {
        TODO("Not yet implemented")
    }

    override suspend fun addAll(vararg move: Move): RoomFlow {
        TODO("Not yet implemented")
    }

    override fun getGame(): GameFlow = callbackFlow {
        trySend(Resource.loading(Constants.FETCHING_DATA))
        try {
            val uid = userPrefs.userPref.first().uid
            val game = gamePrefs.gamePref.first()
            dbRef.child(GAMES).child(uid).child(FRIENDS)
                .child(getFriendUid(game.setId, uid))
                .child(DatabaseConstants.MOVE)
                .observeValue().map { snapshot ->
                    val move = snapshot?.getValue(MoveResponse::class.java)
                    if (move != null) trySend(Resource.success(move.toMove()))
                    else trySend(Resource.error(MoveNotRetrievedException()))
                }
        } catch (e: Exception) {
            trySend(Resource.error(e))
        }
    }

    override suspend fun getAllGames(): GamesFlow {
        TODO("Not yet implemented")
    }
}