package com.esc.test.apps.board.games

import com.esc.test.apps.board.games.data.Game
import com.esc.test.apps.board.games.helpers.GameFlow
import com.esc.test.apps.board.games.helpers.GamesFlow
import com.esc.test.apps.board.moves.data.Move
import com.esc.test.apps.common.helpers.RoomFlow

interface GameDataSource {
    suspend fun add(game: Game): GameFlow
    suspend fun addAll(vararg move: Move): RoomFlow
    fun getGame(): GameFlow
    suspend fun getAllGames(): GamesFlow
}