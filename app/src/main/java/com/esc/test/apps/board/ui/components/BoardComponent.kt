package com.esc.test.apps.board.ui.components

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esc.test.apps.board.moves.data.Move
import com.esc.test.apps.common.components.BaseComponent
import com.esc.test.apps.common.utils.Resource
import com.esc.test.apps.common.utils.collectIn
import com.esc.test.apps.databinding.BoardComponentBinding
import kotlinx.coroutines.flow.Flow

class BoardComponent(
    private val lifecycleOwner: LifecycleOwner,
    binding: BoardComponentBinding,
    OnClearMoves: () -> Unit,
    private val OnCubeClicked: (Move) -> Unit,
    private val InsertMovesToDB: (List<Move>) -> Unit
): BaseComponent<Resource<List<Move>>>(lifecycleOwner) {

    private val layers = ArrayList<RecyclerView>()

    init {
        OnClearMoves()
        var numLayers = 0
        val gridLayoutManager = GridLayoutManager(binding.root.context,4)
        layers.forEach { grid ->
            val cubeAdapter = BoardAdapter(
                OnCubeClicked = {
                    OnCubeClicked(it)
                }
            )
            grid.adapter = cubeAdapter
            grid.layoutManager = gridLayoutManager
            val movePlaceHolders = createMovePlaceHolders(numLayers++)
            cubeAdapter.submitList(movePlaceHolders)
            InsertMovesToDB(movePlaceHolders)
        }
    }

    override fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<Resource<List<Move>>>) {
        dataFlow.collectIn(lifecycleOwner) { resource ->
            val layerList = resource.data?.windowed(16, 16)
            layerList?.let {
                for (i in 0..4) {
                    val adapter = layers[i].adapter as BoardAdapter
                    adapter.submitList(it[i])
                }
            }
        }
    }

    private fun createMovePlaceHolders(z: Int): List<Move> {
        val moves = mutableListOf<Move>()
        var x = 0
        var y = 0
        for (i in 0..16) {
            val cubeCoordinates = "$x$y$z"
            val cubePos = (x * 4 + y + z * 16).toString()
            if (y <= 2) y++
            else {
                x++
                y = 0
            }
            moves.add(Move(cubeCoordinates, cubePos))
        }
        return moves
    }

}