package com.esc.test.apps.common.components

import androidx.lifecycle.LifecycleOwner
import com.esc.test.apps.R
import com.esc.test.apps.common.utils.Resource
import com.esc.test.apps.common.utils.collectIn
import com.esc.test.apps.common.utils.etensions.fadeTo
import com.esc.test.apps.databinding.LoadingComponentBinding
import kotlinx.coroutines.flow.Flow

class LoadingComponent(
    val lifecycleOwner: LifecycleOwner,
    val binding: LoadingComponentBinding
) : BaseComponent<Resource<String>>(lifecycleOwner) {

    override fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<Resource<String>>) {
        visibilityFlow.collectIn(lifecycleOwner) { visible ->
            binding.root.fadeTo(visible)
        }
        dataFlow.collectIn(lifecycleOwner) {
            binding.progressMessage.text = it.data as String? ?: binding.root.context.getString(R.string.loading)
        }
    }
}