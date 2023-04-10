package com.esc.test.apps.board.ui.components

import androidx.lifecycle.LifecycleOwner
import com.esc.test.apps.common.components.BaseComponent
import com.esc.test.apps.common.utils.Resource
import com.esc.test.apps.common.utils.collectIn
import com.esc.test.apps.databinding.ScoreComponentBinding
import kotlinx.coroutines.flow.Flow

class ScoreComponent(
    private val lifecycleOwner: LifecycleOwner,
    private val binding: ScoreComponentBinding,
    private val OnCrossClicked: () -> Unit,
    private val OnCircleClicked: () -> Unit
): BaseComponent<Resource<String>>(lifecycleOwner) {

    override fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<Resource<String>>) {
        dataFlow.collectIn(lifecycleOwner) {
            
        }
    }
}