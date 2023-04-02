package com.esc.test.apps.common.utils.etensions

import android.content.Context
import android.provider.Settings

fun Context.areSystemAnimationsEnabled(): Boolean {
    val duration: Float = Settings.Global.getFloat(
        this.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1F
    )
    val transition: Float = Settings.Global.getFloat(
        this.contentResolver,
        Settings.Global.TRANSITION_ANIMATION_SCALE,
        1F
    )
    return duration != 0f && transition != 0f
}
