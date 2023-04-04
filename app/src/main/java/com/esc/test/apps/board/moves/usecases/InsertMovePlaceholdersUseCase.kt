package com.esc.test.apps.board.moves.usecases

import com.esc.test.apps.board.moves.MoveRepository
import com.esc.test.apps.board.moves.helpers.MoveConstants.NO_OF_LAYERS
import com.esc.test.apps.board.moves.helpers.createMovePlaceHolders
import com.esc.test.apps.board.moves.io.exception.GenerateMovePlaceholdersException
import com.esc.test.apps.common.utils.Resource
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class InsertMovePlaceholdersUseCase @Inject constructor(
    private val repo: MoveRepository
) {
    operator fun invoke() = flow {
        try {
            var layers = 0
            repeat(NO_OF_LAYERS) {
                val moves = layers++.createMovePlaceHolders()
                val list = moves.map { it }.toTypedArray()
                repo.insertAll(*list)
            }
            emit(Resource.success(""))
        } catch (e: IOException) {
            emit(Resource.error(GenerateMovePlaceholdersException()))
        }
    }
}