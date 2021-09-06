package com.esc.test.apps;

import android.app.Activity;
import android.view.View;

import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.LayoutParams;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.esc.test.apps.R;
import com.google.firebase.database.annotations.NotNull;

public class EndDrawerToggle implements DrawerLayout.DrawerListener {

    private final DrawerLayout drawerLayout;
    private final DrawerArrowDrawable arrowDrawable;
    private final AppCompatImageButton toggleButton;
    private final String openDrawerContentDesc;
    private final String closeDrawerContentDesc;

    public EndDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar,
                           int openDrawerContentDescRes, int closeDrawerContentDescRes) {

        this.drawerLayout = drawerLayout;
        this.openDrawerContentDesc = activity.getString(openDrawerContentDescRes);
        this.closeDrawerContentDesc = activity.getString(closeDrawerContentDescRes);

        arrowDrawable = new DrawerArrowDrawable(toolbar.getContext());
        arrowDrawable.setDirection(DrawerArrowDrawable.ARROW_DIRECTION_START);

        toggleButton = new AppCompatImageButton(toolbar.getContext(), null,
                R.attr.toolbarNavigationButtonStyle);
        toolbar.addView(toggleButton, new LayoutParams(GravityCompat.START));
        toggleButton.setImageDrawable(arrowDrawable);
        toggleButton.setOnClickListener(v -> toggle()
        );
    }

    public void syncState() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            setPosition(1f);
        }
        else {
            setPosition(0f);
        }
    }

    private void toggle() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void setPosition(float position) {
        if (position == 1f) {
            arrowDrawable.setVerticalMirror(true);
            toggleButton.setContentDescription(closeDrawerContentDesc);
        }
        else if (position == 0f) {
            arrowDrawable.setVerticalMirror(false);
            toggleButton.setContentDescription(openDrawerContentDesc);
        }
        arrowDrawable.setProgress(position);
    }

    @Override
    public void onDrawerSlide(@NotNull View drawerView, float slideOffset) {
        setPosition(Math.min(1f, Math.max(0, slideOffset)));
    }

    @Override
    public void onDrawerOpened(@NotNull View drawerView) {
        setPosition(1f);
    }

    @Override
    public void onDrawerClosed(@NotNull View drawerView) {
        setPosition(0f);
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }
}