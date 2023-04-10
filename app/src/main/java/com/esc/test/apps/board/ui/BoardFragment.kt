package com.esc.test.apps.board.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.esc.test.apps.R
import com.esc.test.apps.board.ui.components.*
import com.esc.test.apps.databinding.GameFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class BoardFragment: Fragment(R.layout.game_fragment) {

    private lateinit var binding: GameFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = GameFragmentBinding.bind(view)

        TitleComponent(
            this, binding.titleComponent,
            OnQuitClicked = {

            }, OnLevelsClicked = {

            }
        ).collect(emptyFlow(), emptyFlow())

        QuitComponent(this, binding.quitComponent, findNavController())

        GameButtonsComponent(
            this, binding.gameButtonsComponent,
            OnNewGameClicked = {

            }, OnNewSetClicked = {

            }
        ).collect(emptyFlow(), emptyFlow())

        BoardComponent(
            this, binding.boardComponent,
            OnCubeClicked = {

            }, OnClearMoves = {

            }
        ).collect(emptyFlow(), emptyFlow())

        ScoreComponent(
            this, binding.scoreComponent,
            OnCrossClicked = {

            }, OnCircleClicked = {

            }
        ).collect(emptyFlow(), emptyFlow())

    }

}