package com.esc.test.apps.ui.fragments;

import static com.esc.test.apps.utils.AlertType.DISPLAY_NAME;
import static com.esc.test.apps.utils.AlertType.EMAIL;
import static com.esc.test.apps.utils.AlertType.PASSWORD;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

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
        if (Arrays.asList(PASSWORD, EMAIL, DISPLAY_NAME).contains(args.getType())) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext(), R.style.MyDialogTheme);
            dialogBuilder.setView(binding.getRoot());
            binding.title.setText(args.getTitle());
            binding.editText.setHint(args.getMessage());
            return dialogBuilder.create();
        }
        else return new AlertDialog.Builder(requireContext(), R.style.MyDialogTheme)
                .setTitle(args.getTitle()).setMessage(args.getMessage())
                .create();
    }

    private void setListeners() {
        binding.confirm.setOnClickListener(v -> {
            viewModel.checkAction(true);
        });
        binding.editInput.addTextChangedListener(new LetterWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.checkAction(false);
                viewModel.setInput(s.toString());
            }
        });
    }

    private void setObservers() {
        viewModel.error.observe(this, error -> binding.editInput.setError(error));
        viewModel.remoteError.observe(this, error -> binding.editInput.setError(error));
    }
}
