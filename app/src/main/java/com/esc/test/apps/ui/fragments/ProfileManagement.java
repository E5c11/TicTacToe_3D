package com.esc.test.apps.ui.fragments;

import static com.esc.test.apps.utils.Utils.EMAIL_INPUT;
import static com.esc.test.apps.utils.Utils.PASSWORD_INPUT;
import static com.esc.test.apps.utils.Utils.TEXT_INPUT;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.esc.test.apps.R;
import com.esc.test.apps.databinding.ProfileLayoutBinding;
import com.esc.test.apps.utils.LetterWatcher;
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
        binding.displayName.setOnClickListener(v ->
                createAlertDialog("Change display name", "Display name", TEXT_INPUT));
        binding.email.setOnClickListener(v ->
                createAlertDialog("Change email address", "Email address", EMAIL_INPUT));
        binding.password.setOnClickListener(v ->
                createAlertDialog("Change password", "Password", PASSWORD_INPUT));
        binding.delete.setOnClickListener(v -> {
            binding.alert.getRoot().setVisibility(View.VISIBLE);
            binding.alert.editText.setVisibility(View.INVISIBLE);
            binding.alert.text.setVisibility(View.VISIBLE);
        });
        binding.alert.editInput.addTextChangedListener(new LetterWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 3) viewModel.checkDetails(s);
                else binding.alert.confirm.setVisibility(View.INVISIBLE);
            }
        });
        binding.alert.confirm.setOnClickListener(v ->{
            if (binding.alert.title.getText().equals("Change display name"))
                viewModel.changeDisplayName();
            else if (binding.alert.title.getText().equals("Change email address"))
                viewModel.changeEmail();
            else if (binding.alert.title.getText().equals("Change password"))
                viewModel.changePassword();
            else viewModel.deleteAccount();
        });
    }

    private void setObservers() {
        viewModel.getEditTextError().observe(getViewLifecycleOwner(), s -> {
            binding.alert.editText.setError(s);
            if (s.isEmpty()) binding.alert.confirm.setVisibility(View.VISIBLE);
            else binding.alert.confirm.setVisibility(View.INVISIBLE);
        });
    }

    private void createAlertDialog(String title, String hint, int inputType) {
        viewModel.setEditType(inputType);
        binding.alert.getRoot().setVisibility(View.VISIBLE);
        binding.alert.editText.setHint(hint);
        binding.alert.title.setText(title);
        binding.alert.editInput.setInputType(inputType);
    }

}
