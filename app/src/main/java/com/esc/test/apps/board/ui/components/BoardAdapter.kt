package com.esc.test.apps.board.ui.components

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.esc.test.apps.R
import com.esc.test.apps.board.moves.data.Colour
import com.esc.test.apps.board.moves.data.Move
import com.esc.test.apps.board.moves.data.Piece
import com.esc.test.apps.board.moves.data.State
import com.esc.test.apps.databinding.BoardSquareBinding

class BoardAdapter(
    private val OnCubeClicked: (Move) -> Unit
): ListAdapter<Move, BoardAdapter.MoveHolder>( DiffCallback() ) {

    private var screenWidth: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoveHolder {
        val binding = BoardSquareBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MoveHolder(binding)
    }

    override fun onBindViewHolder(holder: MoveHolder, pos: Int) {
        val move = getItem(pos)
        holder.bind(move)
    }

    inner class MoveHolder(private val binding: BoardSquareBinding): RecyclerView.ViewHolder(binding.root) {

        private val confirmColor = ContextCompat.getColor(binding.root.context, R.color.colorTransBlue)

        init {
            if (screenWidth != 0) {
                val metrics: DisplayMetrics = binding.root.context.resources.displayMetrics
                screenWidth = metrics.widthPixels
            }

            binding.root.apply {
                layoutParams = AbsListView.LayoutParams(screenWidth / 10, screenWidth / 10)
            }.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) OnCubeClicked(getItem(position))
            }
        }

        fun bind(move: Move) = binding.root.apply {
            if (tag != null) tag = move
            when (move.state) {
                State.WAITING -> this.setBackgroundColor(confirmColor)
                State.CONFIRMED -> background = ContextCompat.getDrawable(context, getTurnPiece(move))
                State.WINNER -> background = ContextCompat.getDrawable(context, R.drawable.baseline_star_24)
                State.NONE -> Unit
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Move>() {
        override fun areItemsTheSame(oldItem: Move, newItem: Move) =
            (oldItem.id == newItem.id && oldItem.state == newItem.state)

        override fun areContentsTheSame(oldItem: Move, newItem: Move) =
            oldItem == newItem
    }

    private fun getTurnPiece(move: Move): Int = when (move.piecePlayed) {
        Piece.CROSS -> {
            if (Colour.RED == move.color) R.drawable.red_cross
            else R.drawable.black_cross
        }
        else -> {
            if (Colour.RED == move.color) R.drawable.red_circle
            else R.drawable.black_circle
        }
    }


}