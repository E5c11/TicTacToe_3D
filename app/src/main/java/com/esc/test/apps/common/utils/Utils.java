package com.esc.test.apps.common.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.esc.test.apps.R;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.disposables.Disposable;

public class Utils {

    public static final int TEXT_INPUT = 1;
    public static final int PASSWORD_INPUT = 81;
    public static final int EMAIL_INPUT = 21;
    public static final String lookUp = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-d");

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public static List<int[]> addNonNull(List<int[]> list, int[] line){
        if (line != null) list.add(line);
        return list;
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


    public static String getGameUID() {
        return Long.toString(System.currentTimeMillis());
    }

    public static String getGameSetUID(String one, String two, int... i) {
        int index = i[0];
        if (Integer.compare(getLookUpIndex(Character.toUpperCase(one.charAt(index))), getLookUpIndex(Character.toUpperCase(two.charAt(index)))) == -1)
            return one + "_" + two;
        else if(Integer.compare(getLookUpIndex(Character.toUpperCase(one.charAt(index))), getLookUpIndex(Character.toUpperCase(two.charAt(index)))) == 1)
            return two + "_" + one;
        else return getGameSetUID(one, two, ++index);
    }

    private static int getLookUpIndex(char firstChar) { return lookUp.indexOf(firstChar); }

    public static String getDaysAgo(String pastDate) {
        LocalDate now = LocalDate.now();
        LocalDate past = LocalDate.parse(pastDate, formatter);
        return String.valueOf(ChronoUnit.DAYS.between(past, now));
    }

    public static Animation getFlashAnimation() {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500);
        anim.setStartOffset(500);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        return anim;
    }

    public static void showSnackBar(FragmentActivity app, String message, View root){

        Snackbar currentSnackBar = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
        View sbView = currentSnackBar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(app, R.color.colorSignIn));
        currentSnackBar.show();
    }

    public static void dispose(Disposable d) {
        d.dispose();
    }

    public static List<String> setToList(Set<String> set) { return new ArrayList<String>(set);}
}
