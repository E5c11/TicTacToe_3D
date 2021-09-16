package com.esc.test.apps.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.esc.test.apps.viewmodels.LoginViewModel;
import com.esc.test.apps.utils.LetterWatcher;
import com.google.android.material.snackbar.Snackbar;

@AndroidEntryPoint
public class Login extends AppCompatActivity {

    private static final String TAG = "myT";
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

    private void killProgressBar() {
        binding.registerProgress.setAnimation(null);
        binding.registerProgress.setVisibility(View.GONE);
    }

    private void setObservers() {
        loginViewModel.getLoggedIn().observe(this, s -> {
            Log.d(TAG, "setObservers: ");
            if (!s) createNewAccount();
            else startActivity(new Intent(Login.this, PlayWithFriend.class));
        });
        loginViewModel.getPasswordError().observe(this, s -> {
            if (s == null) binding.passConInput.setFocusableInTouchMode(true);
            else binding.password.setError(s);
        });
        loginViewModel.getEmailError().observe(this, s -> binding.email.setError(s));
        loginViewModel.getError().observe(this, s -> {
            killProgressBar();
            if (!s.equals("kill login"))
                Snackbar.make(binding.getRoot(), s, Snackbar.LENGTH_LONG).show();
        });
        loginViewModel.getNetwork().observe(this, s -> {
            if (s != null) {
                if (s) Snackbar.make(
                        binding.getRoot(), "Connection restored", Snackbar.LENGTH_LONG).show();
                else Snackbar.make(
                        binding.getRoot(), "No network connection", Snackbar.LENGTH_INDEFINITE).show();
            }
        });
    }

    private void setRegisterObservers() {
        loginViewModel.getDisplayNameExists().observe(this, s -> binding.displayName.setError(s));
        loginViewModel.getPassConError().observe(this, s -> binding.passCon.setError(s));
    }

    private void createNewAccount() {
        binding.registerProgress.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        binding.loginView.setBackgroundColor(Color.WHITE);
        setListeners();
    }

    private void setListeners() {
        binding.loginText.setOnClickListener(v -> changeLoginSetup());
        binding.emailInput.addTextChangedListener(new LetterWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("@") && s.toString().contains("."))
                    loginViewModel.isEmailValid(s.toString());
            }
        });
        binding.passInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                loginViewModel.setPassword(binding.passInput.getText().toString().trim());
            }
        });
        binding.forgotText.setOnClickListener(v -> {});
        binding.submit.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            if (binding.submit.getText().equals(getResources().getString(R.string.register))) {
                loginViewModel.setDisplayName(binding.displayNameInput.getText().toString().trim());
                loginViewModel.setEmail(email);
                loginViewModel.submitNewUser();
                setProgressBar();
            } else if (binding.submit.getText().equals(getString(R.string.login))) {
                loginViewModel.setEmail(email);
                loginViewModel.setPassword(binding.passInput.getText().toString().trim());
                loginViewModel.loginUser();
                setProgressBar();
            }
        });
    }

    private void setRegisterListeners() {
        binding.displayNameInput.addTextChangedListener(new LetterWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loginViewModel.newDisplayName(s);
            }
        });
        binding.passConInput.addTextChangedListener(new LetterWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loginViewModel.setPassCon(binding.passConInput.getText().toString().trim());
            }
        });
    }

    private void changeLoginSetup() {
        String btnText = binding.submit.getText().toString();
        if (btnText.equals(getResources().getString(R.string.login))) {
            binding.submit.setText(getResources().getString(R.string.register));
            registerViewVisibility(View.VISIBLE);
            binding.forgotText.setVisibility(View.GONE);
            loginViewModel.setLogin(false);
            setRegisterListeners();
            setRegisterObservers();
        } else {
            binding.submit.setText(getResources().getString(R.string.login));
            registerViewVisibility(View.GONE);
            binding.forgotText.setVisibility(View.VISIBLE);
            loginViewModel.setLogin(true);
        }
    }
    private void registerViewVisibility(int vis) {
        binding.displayName.setVisibility(vis);
        binding.passCon.setVisibility(vis);
        binding.regWelTwo.setVisibility(vis);
    }
}
