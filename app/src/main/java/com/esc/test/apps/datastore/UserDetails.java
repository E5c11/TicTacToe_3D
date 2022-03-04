package com.esc.test.apps.datastore;

import android.app.Application;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserDetails {

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    @Inject
    public UserDetails(Application app) {
        pref = app.getSharedPreferences("UserPref", 0);
        editor = pref.edit();
    }

    public void setUid(String uid) {
        editor.putString("uid", uid).commit();
    }
    public String getUid() {
        return pref.getString("uid", null);
    }

    public void setLoggedIn(boolean loggedIn) {
        editor.putBoolean("logged in", loggedIn).commit();
    }
    public boolean isLoggedIn() {
        return pref.getBoolean("logged in", false);
    }

    public void setDisplayName(String name) {
        editor.putString("display name", name).commit();
    }
    public String getDisplayName() {
        return pref.getString("display name", null);
    }

    public void setEmail(String email) {
        editor.putString("user email", email).commit();
    }
    public String getEmail() {
        return pref.getString("user email", null);
    }

    public void setPassword(String password) { editor.putString("user password", password).commit();}
    public String getPassword() {
        return pref.getString("user password", null);
    }

    public void setToken(String token) { editor.putString("user_token", token).commit(); }
    public String getToken() { return pref.getString("user_token", null); }

    public void setLevel(String level) { editor.putString("user_level", level).commit(); }
    public String getLevel() { return pref.getString("user_level", "Normal"); }

    public void clearPrefs() {
        String token = getToken();
        editor.clear().apply();
        setToken(token);
    }
}
