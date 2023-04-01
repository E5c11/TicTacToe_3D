package com.esc.test.apps.board.moves.io.exception

import com.esc.test.apps.common.io.ActionableException
import com.esc.test.apps.common.utils.Constants.NETWORK_CHECK
import java.io.IOException

data class RemoteInsertException(
    val error: Throwable? = IOException(),
    val msg: String = "Unable to save the more to database.",
    val action: String = NETWORK_CHECK
): ActionableException(error, msg, action)
