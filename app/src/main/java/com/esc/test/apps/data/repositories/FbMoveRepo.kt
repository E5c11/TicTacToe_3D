package com.esc.test.apps.data.repositories

import androidx.lifecycle.LiveData
import com.esc.test.apps.board.moves.data.MoveResponse
import com.google.firebase.database.DatabaseReference

interface FbMoveRepo {

    fun addMove(move: MoveResponse)

    fun getMoveInfo(uid: String, gameSetId: String?): LiveData<MoveResponse>?

    fun checkCurrentGameMoves(movesRef: DatabaseReference)

    fun getExistingMoves(): LiveData<List<MoveResponse?>?>?
}