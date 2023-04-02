package com.esc.test.apps.common.components

import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import com.esc.test.apps.R
import com.esc.test.apps.common.helpers.parser.ErrorParser
import com.esc.test.apps.common.utils.Resource
import com.esc.test.apps.common.utils.collectIn
import com.esc.test.apps.common.utils.etensions.fadeTo
import com.esc.test.apps.databinding.ErrorComponentBinding
import kotlinx.coroutines.flow.Flow

class ErrorComponent(
    val lifecycleOwner: LifecycleOwner,
    val binding: ErrorComponentBinding,
    private val errorParser: ErrorParser,
    private val OnPositiveClick: (Intent) -> Unit
) : BaseComponent<Resource<Any>>(lifecycleOwner) {

    override fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<Resource<Any>>) {
        visibilityFlow.collectIn(lifecycleOwner) { visible ->
            binding.root.fadeTo(visible)
        }

        dataFlow.collectIn(lifecycleOwner) { renderError(it.error) }
    }

    private fun renderError(error: Throwable?) = binding.apply {
        val ctx = binding.root.context
        val state = errorParser.parse(error)
        state.apply {
            message.fadeTo(true)
            message.text = msg
            positive.text = posTitle ?: ctx.getString(R.string.okay)
            positive.setOnClickListener {
                root.fadeTo(false)
                state.intent?.let {
                    OnPositiveClick(it)
                }
            }
            if (title == null) errTitle.fadeTo(false)
            else {
                errTitle.fadeTo(true)
                errTitle.text = title
            }
            if (icon == null) errIcon.fadeTo(false)
            else {
                errIcon.fadeTo(true)
                errIcon.setImageResource(icon)
            }
            if (negTitle == null) negative.fadeTo(false)
            else {
                negative.fadeTo(true)
                negative.text = negTitle
                negative.setOnClickListener {
                    root.fadeTo(false)
                }
            }
        }
    }
}
