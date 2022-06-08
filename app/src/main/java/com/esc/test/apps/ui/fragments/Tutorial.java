package com.esc.test.apps.ui.fragments;

import static com.esc.test.apps.adapters.CubeAdapter.getGridAdapter;
import static com.esc.test.apps.utils.TutAction.FLASH;
import static com.esc.test.apps.utils.Utils.getFlashAnimation;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.esc.test.apps.R;
import com.esc.test.apps.adapters.CubeAdapter;
import com.esc.test.apps.databinding.TutorialFragmentBinding;
import com.esc.test.apps.entities.PlayerInstruction;
import com.esc.test.apps.pojos.CubeID;
import com.esc.test.apps.utils.TutAction;
import com.esc.test.apps.viewmodels.TutorialViewModel;

import java.util.ArrayList;

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
            Log.d(TAG, "setTutorial: ");
        });
        Log.d(TAG, "setTutorial: after");
        setObservers();
//        viewModel.startPrompts();
    }

    private void changeSquareIcon(View view) {
        ColorDrawable viewColor = (ColorDrawable) view.getBackground();
        CubeID cube = (CubeID) view.getTag();
        PlayerInstruction pi = viewModel.playerInstruction;
        if (!cube.getArrayPos().equals(pi.getPos()) ||
                (pi.getAltPos() != null && !cube.getArrayPos().equals(pi.getAltPos()))) {
            viewModel.wrongSquare();
        } else {
            if (viewColor == null || viewColor.getColor() != viewModel.confirmColor) {
                String lastPos = viewModel.lastPos;
                if (!lastPos.isEmpty()) removeConfirm(lastPos);
                highlight(view);
                viewModel.lastPos = cube.getArrayPos();
            } else {
                viewModel.lastPos = "";
//            viewModel.updateView(cube);
                updateSquare(cube.getArrayPos(), false);
            }
            viewModel.nextInstruction();
        }
    }

    private void highlight(View view) {
        view.setBackgroundColor(viewModel.confirmColor);
        if (viewModel.playerInstruction.getAction() == FLASH) view.setAnimation(getFlashAnimation());
    }

    private void removeConfirm(String tag) {
        int[] turnPos = getGridAdapter(tag);
        layers.get(turnPos[0]).getChildAt(turnPos[1]).setBackground(null);
    }

    private void setObservers() {
        viewModel.instructionText.observe(getViewLifecycleOwner(), s -> binding.instructions.setText(s));
        viewModel.flash.observe(getViewLifecycleOwner(), instruction -> {
            updateSquare(instruction.getPos(), true);
            if (instruction.getAltPos() != null && !instruction.getAltPos().equals(""))
                updateSquare(instruction.getAltPos(), true);
        });
    }

    private void updateSquare(String tag, boolean animation) {
        int[] turnPos = getGridAdapter(tag);
        layers.get(turnPos[0]).getChildAt(turnPos[1])
                .setBackground(requireContext().getDrawable(R.drawable.baseline_close_24));
        layers.get(turnPos[0]).getChildAt(turnPos[1]).setAnimation(animation? getFlashAnimation() : null);
    }

    private void addLayers() {
        layers.add(binding.front);
        layers.add(binding.frontMiddle);
        layers.add(binding.backMiddle);
        layers.add(binding.back);
    }

}
