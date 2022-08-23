package com.esc.test.apps.ui.fragments;

import static com.esc.test.apps.utils.AlertType.DISPLAY_NAME;
import static com.esc.test.apps.utils.AlertType.EMAIL;
import static com.esc.test.apps.utils.AlertType.PASSWORD;
import static com.esc.test.apps.utils.AlertType.WINNER;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.esc.test.apps.R;
import com.esc.test.apps.databinding.EditTextBinding;
import com.esc.test.apps.utils.LetterWatcher;
import com.esc.test.apps.viewmodels.DialogViewModel;

import java.util.Arrays;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlertDialogFragment extends DialogFragment {

    private DialogViewModel viewModel;
    private EditTextBinding binding;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DialogViewModel.class);
        binding = EditTextBinding.inflate(LayoutInflater.from(requireContext()));
        setListeners();
        setObservers();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialogFragmentArgs args = AlertDialogFragmentArgs.fromBundle(getArguments());
        viewModel.setType(args.getType());
        return dialogSetup(args);
    }

    private Dialog dialogSetup(AlertDialogFragmentArgs args) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext(), R.style.MyDialogTheme);
        dialogBuilder.setView(binding.getRoot());
        binding.title.setText(args.getTitle());
        if (Arrays.asList(PASSWORD, EMAIL, DISPLAY_NAME).contains(args.getType())) {
            binding.editText.setHint(args.getMessage());
        } else {
            binding.confirm.setVisibility(View.VISIBLE);
            binding.editInput.setVisibility(View.GONE);
            binding.text.setVisibility(View.VISIBLE);
            binding.text.setText(args.getMessage());
            if (args.getType().equals(WINNER)) {
                binding.cancel.setVisibility(View.GONE);
                binding.title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
        }
        return dialogBuilder.create();
    }

    private void setListeners() {
        binding.confirm.setOnClickListener(v -> {
            viewModel.checkAction(true);
            binding.progressbar.setVisibility(View.VISIBLE);
//            NavHostFragment.findNavController(this).navigate(AlertDialogFragmentDirections
//                    .actionAlertDialogFragmentToProfileManagement(null));
        });
        binding.cancel.setOnClickListener(v -> dismiss());
        binding.editInput.addTextChangedListener(new LetterWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 3) binding.confirm.setVisibility(View.INVISIBLE);
                else {
                    viewModel.setInput(s.toString());
                    viewModel.checkAction(false);
                }
            }
        });
    }

    private void setObservers() {
        viewModel.error.observe(this, error -> {
            if (error.isEmpty()) binding.confirm.setVisibility(View.VISIBLE);
            else if (error.equals("success") || error.equals(getString(R.string.quit_error)))  {
                binding.progressbar.setVisibility(View.GONE);
                dismiss();
            } else if (error.equals("close")) NavHostFragment.findNavController(this)
                    .navigate(AlertDialogFragmentDirections.actionAlertDialogFragmentToPlayWithFriend());
            else {
                binding.confirm.setVisibility(View.INVISIBLE);
                binding.editInput.setError(error);
            }
        });
        viewModel.quit.observe(this, quit ->
            NavHostFragment.findNavController(this).navigate(AlertDialogFragmentDirections
                .actionAlertDialogFragmentToPlayWithFriend())
        );
    }
}
