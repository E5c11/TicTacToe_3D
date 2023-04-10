package com.esc.test.apps.board.ui.components

import androidx.lifecycle.LifecycleOwner
import com.esc.test.apps.common.components.BaseComponent
import com.esc.test.apps.common.utils.Resource
import com.esc.test.apps.common.utils.collectIn
import com.esc.test.apps.common.utils.etensions.fadeTo
import com.esc.test.apps.databinding.TitleComponentBinding
import kotlinx.coroutines.flow.Flow

class TitleComponent(
    private val lifecycle: LifecycleOwner,
    private val binding: TitleComponentBinding,
    private val OnQuitClicked: () -> Unit,
    private val OnLevelsClicked: () -> Unit
): BaseComponent<Resource<String>>(lifecycle) {

    override fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<Resource<String>>) {
        visibilityFlow.collectIn(lifecycle) {
            binding.apply {
//                binding.quit.fadeTo()
            }
        }

        dataFlow.collectIn(lifecycle) {
            binding.title.text = it.data
        }
    }
}