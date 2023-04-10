package com.esc.test.apps.board.games.helpers

import com.esc.test.apps.board.games.data.Game
import com.esc.test.apps.board.games.data.GameEntity
import com.esc.test.apps.common.utils.Resource
import kotlinx.coroutines.flow.Flow

typealias GameFlow = Flow<Resource<Game>>
typealias GamesFlow = Flow<Resource<List<Game>>>
typealias GameEntityFlow = Flow<Resource<GameEntity>>
typealias GamesEntityFlow = Flow<Resource<List<GameEntity>>>