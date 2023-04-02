package com.esc.test.apps.common.helpers.parser

import android.content.Context
import com.esc.test.apps.R
import com.esc.test.apps.board.moves.io.exception.MoveNotRetrievedException
import com.esc.test.apps.common.data.ErrorState
import javax.inject.Inject

class ErrorParser @Inject constructor(private val context: Context) {

    fun parse(error: Throwable?): ErrorState {
        return if (error == null) {
            ErrorState(msg = context.getString(R.string.general_error), title = context.getString(R.string.general_error_title))
        } else {
            when (error) {
                is MoveNotRetrievedException -> ErrorState(
                    msg = context.getString(R.string.no_moves_error),
                    title = context.getString(R.string.no_moves_error_title),
                    posTitle = context.getString(R.string.okay),
                )
                else -> ErrorState(
                    msg = context.getString(R.string.general_error),
                    title = context.getString(R.string.general_error_title)
                )
            }
        }
    }
}
