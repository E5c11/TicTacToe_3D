package com.esc.test.apps.ui.fragments;

import static com.esc.test.apps.utils.AlertType.DELETE;
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
import com.esc.test.apps.utils.AlertType;
import com.esc.test.apps.viewmodels.ProfileViewModel;
import com.google.android.material.snackbar.Snackbar;

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

        setUi();
        setListeners();
        setObservers();
    }

    private void setUi() {
        binding.displayName.text.setText(getString(R.string.change_display_name));
        binding.email.text.setText(getString(R.string.change_email));
        binding.password.text.setText(getString(R.string.change_password));
        binding.delete.text.setText(getString(R.string.delete_account));
    }

    private void setListeners() {
        binding.displayName.getRoot().setOnClickListener(v -> navigateTo(getString(R.string.change_display_name),
                getString(R.string.display_name_asterisk), DISPLAY_NAME));
        binding.email.getRoot().setOnClickListener(v -> navigateTo(getString(R.string.change_email),
                getString(R.string.email_asterisk), EMAIL));
        binding.password.getRoot().setOnClickListener(v -> navigateTo(getString(R.string.change_password),
                getString(R.string.passcon_asterisk), PASSWORD));
        binding.delete.getRoot().setOnClickListener(v -> navigateTo(getString(R.string.delete_account),
                getString(R.string.delete_account_message), DELETE));
    }

    private void setObservers() {
        viewModel.getNetwork().observe(getViewLifecycleOwner(), s -> {
            if (s != null) {
                if (s) Snackbar.make(
                        binding.getRoot(), getString(R.string.network_restored), Snackbar.LENGTH_LONG).show();
                else Snackbar.make(
                        binding.getRoot(), getString(R.string.network_lost), Snackbar.LENGTH_INDEFINITE).show();
            }
        });
    }

    private void navigateTo(String title, String message, AlertType type) {
        NavHostFragment.findNavController(this).navigate(
                ProfileManagementDirections.actionGlobalAlertDialogFragment(
                        title, message, type));
    }

}
