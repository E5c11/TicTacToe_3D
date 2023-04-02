package com.esc.test.apps.common.components

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.Flow

/**
 * Self contained & reusable ui component.This is a lifecycle aware component.
 * It makes sure we don't clutter an android Fragment/Activity with ui logic.
 */
@Suppress("LeakingThis")
abstract class BaseComponent<T>(val owner: LifecycleOwner) : DefaultLifecycleObserver {

    init {
        owner.lifecycle.addObserver(this)
    }

    abstract fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<T>)

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        onStart()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        onPause()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        onStop()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        onDestroy()
    }

    open fun onStart() {}

    open fun onResume() {}

    open fun onPause() {}

    open fun onStop() {}

    open fun onDestroy() {}
}