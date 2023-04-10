package com.esc.test.apps.board.usecase

import com.esc.test.apps.board.games.GameRepository
import javax.inject.Inject

class NewGameUseCase @Inject constructor(
    private val gameRepo: GameRepository,
) {

    operator fun invoke() = { }

}