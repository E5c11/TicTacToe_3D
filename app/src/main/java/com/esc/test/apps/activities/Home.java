package com.esc.test.apps.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.esc.test.apps.BaseActivity;
import com.esc.test.apps.R;
import com.google.firebase.messaging.FirebaseMessaging;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class Home extends BaseActivity {

    private Button button;
    private TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.home_activity, baseLayout);

        button = findViewById(R.id.home_button);
        login = findViewById(R.id.login_text);

        setButton();
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
    }

    private void setButton() {
        final Animation an1 = AnimationUtils.loadAnimation(this, R.anim.rotate);
        button.setOnClickListener(v -> {
            v.startAnimation(an1);
            startActivity(new Intent(this, BoardActivity.class));
        });
        login.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
        });
    }
}
