package com.esc.test.apps.board.moves.helpers

import com.esc.test.apps.board.moves.data.Move
import com.esc.test.apps.board.moves.data.MoveEntity
import com.esc.test.apps.common.utils.Resource
import kotlinx.coroutines.flow.Flow

typealias MoveEntityFlow = Flow<Resource<MoveEntity>>
typealias MovesEntityFlow = Flow<Resource<List<MoveEntity>>>
typealias MoveFlow = Flow<Resource<Move>>
typealias MovesFlow = Flow<Resource<List<Move>>>