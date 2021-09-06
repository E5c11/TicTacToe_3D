package com.esc.test.apps.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.esc.test.apps.R;
import com.esc.test.apps.databinding.LoginActivityBinding;

import dagger.hilt.android.AndroidEntryPoint;
import com.esc.test.apps.modelviews.LoginViewModel;

@AndroidEntryPoint
public class Login extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private LoginActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LoginActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d("myT", "LoginActivity");
        setProgressBar();
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        setObservers();
    }

    private void setProgressBar() {
        final Animation an = AnimationUtils.loadAnimation(this, R.anim.rotate_fast);
        binding.registerProgress.setAnimation(an);
    }

    private void setObservers() {
        loginViewModel.getLoggedIn().observe(this, s -> {
            if (!s) createNewAccount();
            else startActivity(new Intent(Login.this, PlayWithFriend.class));
        });
        loginViewModel.getDisplayNameExists().observe(this, s -> {
                if (s) binding.displayName.setError("Display name taken, choose another");
                else binding.displayName.setError(null);
                });
        loginViewModel.getDisplayNameError().observe(this, s -> binding.displayNameInput.setError(s));
        loginViewModel.getPasswordError().observe(this, s -> {
            if (s == null) binding.passConInput.setFocusableInTouchMode(true);
            else binding.password.setError(s);
        });
        loginViewModel.getEmailError().observe(this, s -> binding.email.setError(s));
        loginViewModel.getPassConError().observe(this, s -> binding.passConInput.setError(s));
        loginViewModel.getChangePassFocus().observe(this, s -> {
            if (!s) binding.passInput.setFocusable(true);
        });
    }

    private void createNewAccount() {
        binding.registerProgress.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        binding.loginView.setBackgroundColor(Color.WHITE);
        setListeners();
    }

    private void setListeners() {
        binding.loginText.setOnClickListener(v -> enableLogin());
        binding.emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("@") && s.toString().contains("."))
                    loginViewModel.isEmailValid(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        binding.displayNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                loginViewModel.newDisplayName(s);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        binding.passInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                loginViewModel.setPassword(binding.passInput.getText().toString().trim());
            }
        });
        binding.passConInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                loginViewModel.setPassword(binding.passInput.getText().toString().trim());
            }
        });
        binding.submit.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            if (binding.submit.getText().equals(getResources().getString(R.string.register))) {
                loginViewModel.setDisplayName(binding.displayNameInput.getText().toString().trim());
                loginViewModel.setEmail(email);
                loginViewModel.setPassCon(binding.passConInput.getText().toString().trim());
                loginViewModel.submitNewUser();
            } else {
                if (binding.password.getError() == null) {
                    String pass = binding.passInput.getText().toString().trim();
                    loginViewModel.setPassword(pass);
                    loginViewModel.getUserDetails(email, pass);
                    setProgressBar();
                }
            }
        });
    }

    private void enableLogin() {
        binding.displayNameInput.setVisibility(View.GONE);
        binding.passConInput.setVisibility(View.GONE);
        binding.submit.setText(getResources().getString(R.string.login));
    }
}
