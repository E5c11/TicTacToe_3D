package com.esc.test.apps.ui.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.esc.test.apps.R;
import com.esc.test.apps.viewmodels.DialogViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlertDialogFragment extends DialogFragment {

    private DialogViewModel viewModel;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DialogViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialogFragmentArgs args = AlertDialogFragmentArgs.fromBundle(getArguments());
        viewModel.setType(args.getType());
        return new AlertDialog.Builder(requireContext())
                .setTitle(args.getTitle()).setMessage(args.getMessage())
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                    if (which == DialogInterface.BUTTON_POSITIVE)
                        viewModel.checkAction();
                })
                .create();
    }

}
