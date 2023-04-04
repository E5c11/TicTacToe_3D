package com.esc.test.apps.board.ui.components

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esc.test.apps.board.moves.data.Move
import com.esc.test.apps.board.moves.helpers.MoveConstants.NO_CUBES_PER_LAYER
import com.esc.test.apps.board.moves.helpers.MoveConstants.NO_OF_LAYERS
import com.esc.test.apps.common.components.BaseComponent
import com.esc.test.apps.common.utils.Resource
import com.esc.test.apps.common.utils.collectIn
import com.esc.test.apps.databinding.BoardComponentBinding
import kotlinx.coroutines.flow.Flow

class BoardComponent(
    private val lifecycleOwner: LifecycleOwner,
    binding: BoardComponentBinding,
    OnClearMoves: () -> Unit,
    private val OnCubeClicked: (Move) -> Unit
): BaseComponent<Resource<List<Move>>>(lifecycleOwner) {

    private val layers = ArrayList<RecyclerView>()

    init {
        OnClearMoves()
        val gridLayoutManager = GridLayoutManager(binding.root.context, NO_OF_LAYERS)
        layers.forEach { grid ->
            val cubeAdapter = BoardAdapter(
                OnCubeClicked = {
                    OnCubeClicked(it)
                }
            )
            grid.adapter = cubeAdapter
            grid.layoutManager = gridLayoutManager
        }
    }

    override fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<Resource<List<Move>>>) {
        dataFlow.collectIn(lifecycleOwner) { resource ->
            val layerList = resource.data?.windowed(NO_CUBES_PER_LAYER, NO_CUBES_PER_LAYER)
            layerList?.let {
                for (i in 0..NO_OF_LAYERS) {
                    val adapter = layers[i].adapter as BoardAdapter
                    adapter.submitList(it[i])
                }
            }
        }
    }

}