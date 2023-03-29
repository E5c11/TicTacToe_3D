package com.esc.test.apps.common.io

open class ActionableException(
    error: Throwable? = null,
    msg: String? = null,
    action: String? = null
) : Exception(msg, error)