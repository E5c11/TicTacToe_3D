package com.esc.test.apps.board.ui.components

import androidx.lifecycle.LifecycleOwner
import com.esc.test.apps.common.components.BaseComponent
import com.esc.test.apps.common.utils.Resource
import com.esc.test.apps.common.utils.collectIn
import com.esc.test.apps.databinding.GameButtonsComponentBinding
import kotlinx.coroutines.flow.Flow

class GameButtonsComponent(
    private val lifecycleOwner: LifecycleOwner,
    private val binding: GameButtonsComponentBinding,
    private val OnNewGameClicked: () -> Unit,
    private val OnNewSetClicked: () -> Unit
): BaseComponent<Resource<String>>(lifecycleOwner) {

    override fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<Resource<String>>) {
        dataFlow.collectIn(lifecycleOwner) {
            binding.newGame.setOnClickListener {
                OnNewGameClicked()
            }
            binding.newSet.setOnClickListener {
                OnNewSetClicked()
            }
        }
    }
}