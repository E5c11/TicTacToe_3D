package com.esc.test.apps.board.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.esc.test.apps.R
import com.esc.test.apps.board.ui.components.GameButtonsComponent
import com.esc.test.apps.databinding.GameFragmentBinding
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class BoardFragment: Fragment(R.layout.game_fragment) {

    private lateinit var binding: GameFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = GameFragmentBinding.bind(view)



        GameButtonsComponent(
            this, binding.gameButtonsComponent,
            OnNewGameClicked = {

            }, OnNewSetClicked = {

            }
        ).collect(emptyFlow(), emptyFlow())




    }

}