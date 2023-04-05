package com.esc.test.apps.ui.fragments;

import static com.esc.test.apps.common.adaptors.CubeAdapter.getGridAdapter;
import static com.esc.test.apps.tutorial.helpers.TutAction.FLASH;
import static com.esc.test.apps.common.utils.Utils.getFlashAnimation;
import static com.esc.test.apps.domain.viewmodels.board.PlayAIViewModel.AI_GAME;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.esc.test.apps.R;
import com.esc.test.apps.common.adaptors.CubeAdapter;
import com.esc.test.apps.databinding.TutorialFragmentBinding;
import com.esc.test.apps.data.models.entities.PlayerInstruction;
import com.esc.test.apps.data.models.pojos.CubeID;
import com.esc.test.apps.domain.viewmodels.TutorialViewModel;

import java.util.ArrayList;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class Tutorial extends Fragment {

    public Tutorial() { super(R.layout.tutorial_fragment); }

    private TutorialViewModel viewModel;
    private TutorialFragmentBinding binding;
    private final ArrayList<GridView> layers = new ArrayList<>();
    private int numLayers;
    private static final String TAG = "myT";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = TutorialFragmentBinding.bind(view);
        Log.d("myT", "TutorialActivity");
        viewModel = new ViewModelProvider(this).get(TutorialViewModel.class);
        setBoard();
    }
    private void setBoard() {
//        setPieceClickEnabled();
        if (layers.isEmpty()) addLayers();
        numLayers = 0;
        layers.forEach(i -> {
            viewModel.setCubes(numLayers);
            CubeAdapter cubeAdapter = new CubeAdapter(requireContext(), viewModel.getLayerIDs().get(numLayers));
            i.setAdapter(cubeAdapter);
            i.setOnItemClickListener((adapterView, view, j, l) -> changeSquareIcon(view));
            numLayers++;
        });
        setObservers();
    }

    private void changeSquareIcon(View view) {
        ColorDrawable viewColor = (ColorDrawable) view.getBackground();
        CubeID cube = (CubeID) view.getTag();
        PlayerInstruction pi = viewModel.playerInstruction;
        //Check wrong square
        if (((!Objects.equals(cube.getArrayPos(), pi.getPos()) && !Objects.equals(cube.getArrayPos(), pi.getAltPos())) ||
                (!Objects.equals(cube.getArrayPos(), pi.getAltPos())) && !Objects.equals(cube.getArrayPos(), pi.getPos())) ||
                (!Objects.equals(cube.getArrayPos(), pi.getPos()) && pi.getAltPos() == null)
                && viewColor == null) {
            viewModel.wrongSquare();
            if (Objects.equals(viewModel.line, "")) {
                hint(pi.getPos());
                altHint(pi.getAltPos());
            } else if (Objects.equals(viewModel.line, "second")) altHint(pi.getAltPos());
            else hint(pi.getPos());
        } else {
            //Check confirm square
            if (viewColor == null || viewColor.getColor() != viewModel.confirmColour) {
                if (Objects.equals(cube.getArrayPos(), pi.getAltPos())) {
                    removeConfirm(viewModel.lastAltPos);
                    viewModel.lastAltPos = cube.getArrayPos();
                } else {
                    removeConfirm(viewModel.lastPos);
                    viewModel.lastPos = cube.getArrayPos();
                }
                highlight(view);
                viewModel.nextInstruction(false);
            } else {                                        //Update view with move
                if (!viewModel.lastAltPos.isEmpty()) {
                    removeConfirm(viewModel.lastAltPos);
                    removeConfirm(viewModel.lastPos);
                    if (Objects.equals(viewModel.line, "")) viewModel.line = "second";
                } else {
                    if (Objects.equals(viewModel.line, "")) viewModel.line = "first";
                    removeConfirm(viewModel.lastPos);
                }
                viewModel.lastPos = "";
                viewModel.lastAltPos = "";
                updateSquare(cube.getArrayPos(), false);
                viewModel.nextInstruction(true);
            }
        }
    }

    private void hint(String pos) {
        updateSquare(pos, true);
        viewModel.lastPos = pos;
    }

    private void altHint(String pos) {
        updateSquare(pos, true);
        viewModel.lastAltPos = pos;
    }

    private void highlight(View view) {
        view.setBackgroundColor(viewModel.confirmColour);
        if (viewModel.playerInstruction.getAction() == FLASH) view.setAnimation(getFlashAnimation());
    }

    private void removeConfirm(String tag) {
        if (!tag.isEmpty()) {
            int[] turnPos = getGridAdapter(tag);
            layers.get(turnPos[0]).getChildAt(turnPos[1]).setBackground(null);
        }
    }

    private void setObservers() {
        viewModel.instructionText.observe(getViewLifecycleOwner(), s -> binding.instructions.setText(s));
        viewModel.flash.observe(getViewLifecycleOwner(), instruction -> {
            updateSquare(instruction.getPos(), true);
            viewModel.lastPos = instruction.getPos();
        });
        viewModel.pcMove.observe(getViewLifecycleOwner(), move -> {
            int[] turnPos = getGridAdapter(move);
            layers.get(turnPos[0]).getChildAt(turnPos[1])
                    .setBackground(requireContext().getDrawable(R.drawable.black_circle));
            layers.get(turnPos[0]).getChildAt(turnPos[1]).setOnClickListener(null);
        });
        viewModel.winner.observe(getViewLifecycleOwner(), line -> {
            for (String move : line) {
                int[] turnPos = getGridAdapter(move);
                layers.get(turnPos[0]).getChildAt(turnPos[1])
                        .setBackground(requireContext().getDrawable(R.drawable.win_star));
                layers.get(turnPos[0]).getChildAt(turnPos[1]).setOnClickListener(null);
            }
        });
        viewModel.restart.observe(getViewLifecycleOwner(), s -> setBoard());
        viewModel.end.observe(getViewLifecycleOwner(), end -> {
            NavDirections action = TutorialDirections.actionTutorialToBoardActivity(AI_GAME, "from_tut");
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });
    }

    private void updateSquare(String tag, boolean animation) {
        int[] turnPos = getGridAdapter(tag);
        layers.get(turnPos[0]).getChildAt(turnPos[1])
                .setBackground(requireContext().getDrawable(
                        animation ? R.color.colorTransBlue : R.drawable.black_cross));
        layers.get(turnPos[0]).getChildAt(turnPos[1]).setAnimation(animation ? getFlashAnimation() : null);
        if (!animation) layers.get(turnPos[0]).getChildAt(turnPos[1]).setOnClickListener(null);
    }

    private void addLayers() {
        layers.add(binding.front);
        layers.add(binding.frontMiddle);
        layers.add(binding.backMiddle);
        layers.add(binding.back);
    }

}
