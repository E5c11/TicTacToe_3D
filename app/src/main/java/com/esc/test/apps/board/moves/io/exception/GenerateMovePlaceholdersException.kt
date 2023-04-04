package com.esc.test.apps.board.moves.io.exception

import com.esc.test.apps.common.io.ActionableException
import java.io.IOException

data class GenerateMovePlaceholdersException(
    val error: Throwable = IOException(),
    val msg: String = "Error when inserting placeholders"
): ActionableException(error, msg)
