package com.esc.test.apps.utils;

import android.view.View;
import android.view.ViewGroup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static final int TEXT_INPUT = 1;
    public static final int PASSWORD_INPUT = 81;
    public static final int EMAIL_INPUT = 21;

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public static boolean validEmail(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) return "Password cannot be empty";
        else if (password.equals(password.toLowerCase()) && !password.matches(".*\\d.*"))
            return "Password must contain an uppercase and number";
        else if (password.equals(password.toLowerCase())) return "Password must contain an uppercase";
        else if (!password.matches(".*\\d.*")) return "Password must contain a number";
        else if (!(password.length() >= 6)) return "Password must contain at least 6 characters";
        else return "";
    }

}
