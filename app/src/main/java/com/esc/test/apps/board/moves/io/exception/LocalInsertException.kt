package com.esc.test.apps.board.moves.io.exception

import com.esc.test.apps.common.io.ActionableException
import com.esc.test.apps.common.utils.Constants
import java.io.IOException

data class LocalInsertException(
    val error: Throwable? = IOException(),
    val msg: String = "Unable to save the more to database.",
    val action: String = Constants.NETWORK_CHECK
): ActionableException(error, msg, action)
