package com.esc.test.apps.board.ui.components

import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.esc.test.apps.R
import com.esc.test.apps.board.ui.BoardFragmentDirections
import com.esc.test.apps.common.components.BaseComponent
import com.esc.test.apps.common.utils.AlertType
import com.esc.test.apps.common.utils.Resource
import com.esc.test.apps.common.utils.collectIn
import com.esc.test.apps.common.utils.etensions.fadeTo
import com.esc.test.apps.databinding.QuitComponentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow

@AndroidEntryPoint
class QuitComponent(
    private val lifecycleOwner: LifecycleOwner,
    private val binding: QuitComponentBinding,
    private val navController: NavController
): BaseComponent<Resource<String>>(lifecycleOwner) {

    init {
        binding.quit.setOnClickListener {
            navController.navigate(BoardFragmentDirections.actionBoardFragmentToAlertDialogFragment(
                binding.root.context.getString(R.string.confirm_quit),
                binding.root.context.getString(R.string.quit_msg),
                AlertType.QUIT
            ))
        }
    }

    override fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<Resource<String>>) {
        visibilityFlow.collectIn(lifecycleOwner) {
            binding.quit.fadeTo(it)
        }
    }
}