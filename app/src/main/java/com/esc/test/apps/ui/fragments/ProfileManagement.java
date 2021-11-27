package com.esc.test.apps.ui.fragments;

import static com.esc.test.apps.utils.AlertType.DISPLAY_NAME;
import static com.esc.test.apps.utils.AlertType.EMAIL;
import static com.esc.test.apps.utils.AlertType.PASSWORD;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.esc.test.apps.R;
import com.esc.test.apps.databinding.ProfileLayoutBinding;
import com.esc.test.apps.viewmodels.ProfileViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileManagement extends Fragment {

    public ProfileManagement() { super(R.layout.profile_layout); }

    private ProfileLayoutBinding binding;
    private ProfileViewModel viewModel;
    private static final String TAG = "myT";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = ProfileLayoutBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setListeners();
        setObservers();
    }

    private void setListeners() {
        binding.displayName.setOnClickListener(v -> {
                        ;
                NavHostFragment.findNavController(this)
                        .navigate(ProfileManagementDirections.actionGlobalAlertDialogFragment(
                        "Change display name", "Display name", DISPLAY_NAME));
        });
        binding.email.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(ProfileManagementDirections.actionGlobalAlertDialogFragment(
                    "Change email address", "Email address", EMAIL));
        });
        binding.password.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(ProfileManagementDirections.actionGlobalAlertDialogFragment(
                    "Change password", "Password", PASSWORD));
        });
        binding.delete.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(
                    ProfileManagementDirections.actionGlobalAlertDialogFragment(
                    "Delete account", "Are you sure you would like to delete your account?", PASSWORD));
        });
    }

    private void setObservers() {

    }

}
