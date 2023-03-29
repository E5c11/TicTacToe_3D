package com.esc.test.apps.common.network.io

import com.esc.test.apps.common.io.ActionableException

class FirebaseException(
    val error: Throwable? = null,
    val msg: String = "A Firebase error has occurred"
): ActionableException(error, msg)