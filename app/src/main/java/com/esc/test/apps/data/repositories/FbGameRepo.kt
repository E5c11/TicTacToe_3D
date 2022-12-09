package com.esc.test.apps.data.repositories

import androidx.lifecycle.LiveData
import com.esc.test.apps.common.utils.SingleLiveEvent
import com.esc.test.apps.data.models.pojos.UserInfo
import io.reactivex.rxjava3.subjects.PublishSubject

interface FbGameRepo {

    val newFriend: SingleLiveEvent<UserInfo>
    val startGame: SingleLiveEvent<Array<String>>
    val quit: SingleLiveEvent<Boolean>
    val error: SingleLiveEvent<String>
    val gameId: PublishSubject<String>

    fun findFriend(friend_name: String)

    fun acceptInvite(user: UserInfo)

    fun startGame(user: UserInfo, firstPlayer: Boolean)

    fun sendGameInvite(user: UserInfo, startGame: Boolean)

    fun inviteNewFriend()

    fun endGame(winner: String?)

    fun getGameUID(uids: String)

    fun getActiveFriends(uid: String?): LiveData<List<UserInfo>>

    fun getFriendRequests(uid: String?): LiveData<List<UserInfo>>

    fun setGameActiveState(gameId: String)

    fun getGameActiveState(): LiveData<Map<String, String>>
}