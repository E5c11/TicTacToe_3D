package com.esc.test.apps.board.moves.io.exception

import com.esc.test.apps.common.io.ActionableException

class MoveNotRetrievedException(
    val error: Throwable? = null,
    val msg: String = "Move was not retrieved"
): ActionableException(error, msg)