package com.esc.test.apps.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.esc.test.apps.databinding.ProfileLayoutBinding;
import com.esc.test.apps.utils.LetterWatcher;
import com.esc.test.apps.utils.Utils;
import com.esc.test.apps.viewmodels.ProfileViewModel;

import org.w3c.dom.Text;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileManagement extends AppCompatActivity {

    private ProfileLayoutBinding binding;
    private ProfileViewModel viewModel;
    private AlertDialog.Builder alert;
    private static final String TAG = "myT";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ProfileLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setListeners();
        setObservers();
    }

    private void setListeners() {
        binding.displayName.setOnClickListener(v ->
                createAlertDialog("Change display name", "Display name", Utils.TEXT_INPUT));
        binding.alert.editInput.addTextChangedListener(new LetterWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 3) viewModel.checkDetails(s);
                else binding.alert.confirm.setVisibility(View.INVISIBLE);
            }
        });
        binding.alert.confirm.setOnClickListener(v ->
                viewModel.checkDetails(binding.alert.editInput.getText()));
    }

    private void setObservers() {
        viewModel.getEditTextError().observe(this, s -> {
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
