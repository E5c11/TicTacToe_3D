package com.esc.test.apps.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.esc.test.apps.R;
import com.esc.test.apps.databinding.HomeActivityBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class Home extends Fragment {

    public Home() { super(R.layout.home_activity); }

    private HomeActivityBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = HomeActivityBinding.bind(view);
        setButton();
    }

    private void setButton() {
        final Animation an1 = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate);
        binding.passPlay.setOnClickListener(v -> {
            v.startAnimation(an1);
            NavDirections action = HomeDirections.actionHomeToBoardActivity(null, null);
            Navigation.findNavController(v).navigate(action);
        });
        binding.playAi.setOnClickListener(v -> {
            NavDirections action = HomeDirections.actionHomeToBoardActivity("play_ai", null);
            Navigation.findNavController(v).navigate(action);
        });
        binding.playFriend.setOnClickListener(v -> {
            NavDirections action = HomeDirections.actionHomeToLogin();
            Navigation.findNavController(v).navigate(action);
        });
        binding.manageProfile.setOnClickListener(v -> {
            v.startAnimation(an1);
            NavDirections action = HomeDirections.actionHomeToProfileManagement();
            Navigation.findNavController(v).navigate(action);
        });
        onBackPressed();
    }

    private void onBackPressed() {
        requireActivity().getOnBackPressedDispatcher()
            .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    Intent a = new Intent(Intent.ACTION_MAIN);
                    a.addCategory(Intent.CATEGORY_HOME);
                    a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(a);
                }
            });
    }
}
