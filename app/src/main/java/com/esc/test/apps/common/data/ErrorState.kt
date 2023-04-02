package com.esc.test.apps.common.data

import android.content.Intent

data class ErrorState(
    val msg: String,
    val title: String? = null,
    val icon: Int? = null,
    val posTitle: String? = null,
    val negTitle: String? = null,
    val intent: Intent? = null
)
