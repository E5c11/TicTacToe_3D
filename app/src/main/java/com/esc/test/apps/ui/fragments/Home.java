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
import com.esc.test.apps.datastore.UserDetails;
import com.esc.test.apps.utils.AlertType;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class Home extends Fragment {

    public Home() { super(R.layout.home_activity); }

    private HomeActivityBinding binding;

    @Inject
    UserDetails user;

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
            NavDirections action;
            if (user.getTutorial()) {
                action = HomeDirections.actionHomeToTutorial();
            } else {
                action = HomeDirections.actionHomeToBoardActivity(null, null);
            }
            Navigation.findNavController(v).navigate(action);
        });
        binding.playAi.setOnClickListener(v -> {
            NavDirections action = HomeDirections.actionHomeToBoardActivity("play_ai", null);
            Navigation.findNavController(v).navigate(action);
        });
        binding.playFriend.setOnClickListener(v -> goToLogin(v, null));
        binding.manageProfile.setOnClickListener(v -> {
            if (user.getUid() != null) {
                v.startAnimation(an1);
                NavDirections action = HomeDirections.actionHomeToProfileManagement(AlertType.DISPLAY_NAME);
                Navigation.findNavController(v).navigate(action);
            } else {
                goToLogin(v, "profile");
                Snackbar.make(binding.getRoot(), "Please login first", Snackbar.LENGTH_SHORT).show();
            }
        });
        onBackPressed();
    }

    private void goToLogin(View v, String goTo) {
        NavDirections action = HomeDirections.actionHomeToLogin(goTo);
        Navigation.findNavController(v).navigate(action);
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
