package com.esc.test.apps.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.esc.test.apps.R;
import com.esc.test.apps.databinding.HomeActivityBinding;
import com.google.firebase.messaging.FirebaseMessaging;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class Home extends AppCompatActivity {

    private HomeActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        binding = HomeActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setButton();
    }

    private void setButton() {
        final Animation an1 = AnimationUtils.loadAnimation(this, R.anim.rotate);
        binding.passPlay.setOnClickListener(v -> {
            v.startAnimation(an1);
            startActivity(new Intent(this, BoardActivity.class));
        });
        binding.playAi.setOnClickListener(v -> {
            Intent i = new Intent(this, BoardActivity.class);
            i.putExtra("play_ai", "play_ai");
            startActivity(i);
        });
        binding.playFriend.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
        });
        binding.manageProfile.setOnClickListener(v -> {
            v.startAnimation(an1);
            startActivity(new Intent(this, ProfileManagement.class));
        });
    }

    @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
