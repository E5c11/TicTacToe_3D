package com.esc.test.apps.data.repositories

import androidx.lifecycle.LiveData
import com.esc.test.apps.data.objects.pojos.MoveInfo
import com.google.firebase.database.DatabaseReference

interface FbMoveRepo {

    fun addMove(move: MoveInfo)

    fun getMoveInfo(uid: String, gameSetId: String?): LiveData<MoveInfo>?

    fun checkCurrentGameMoves(movesRef: DatabaseReference)

    fun getExistingMoves(): LiveData<List<MoveInfo?>?>?
}