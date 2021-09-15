package com.esc.test.apps.datastore;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.esc.test.apps.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GameState {

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Application app;
    private final ArrayList<String> winnerLine = new ArrayList<>();

    @Inject
    public GameState(Application app) {
        this.app = app;
        pref = app.getSharedPreferences("MyPref", 0);
        editor = pref.edit();
    }

    public String isWinner() {
        return pref.getString("winner", null);
    }
    public void setWinner(String winner) {
        editor.putString("winner", winner).commit();
    }

    public ArrayList<String> getWinnerLine() {
        if (pref.contains("winnerLine")) {
            Set<String> set = pref.getStringSet("winnerLine", null);
            winnerLine.clear();
            winnerLine.addAll(set);
        }
        return winnerLine;
    }
    public void setWinnerLine(ArrayList<String> winnerLine) {
        Set<String> set = new HashSet<>(winnerLine);
        editor.putStringSet("winnerLine", set).commit();
    }

    public void clearWinnerLine() {
        setWinnerLine(new ArrayList<>());
        setWinner(null);
    }

    public void newGame() {
        String circle, cross, starter;
        setWinner(null);
        clearWinnerLine();
        if (getStarter() != null) {
            if (getStarter().equals(app.getString(R.string.circle))) starter = app.getString(R.string.cross);
            else starter = app.getString(R.string.circle);
        } else starter = app.getString(R.string.cross);
        Log.d("myT", "new game");
        circle = getCircleScore();
        cross = getCrossScore();
        editor.clear().commit();
        setCircleScore(circle);
        setCrossScore(cross);
        setStarter(starter);
    }

    public void newSet() {
        editor.clear().commit();
        newGame();
    }

    public void setStarter(String starter) {editor.putString("starter", starter).commit();}
    public String getStarter() {return pref.getString("starter", null);}

    public void setCircleScore(String circleScore) {editor.putString("circle_score", circleScore).commit();}
    public String getCircleScore() { return pref.getString("circle_score", "0"); }

    public void setCrossScore(String crossScore) { editor.putString("cross_score", crossScore).commit(); }
    public String getCrossScore() { return pref.getString("cross_score", "0"); }

    public void setGameID(String gameID) { editor.putString("game_id", gameID).commit(); }
    public String getGameID() { return pref.getString("game_id", null); }

    public void setGameSetID(String gameSetID) {editor.putString("game_set_id", gameSetID).commit();}
    public String getGameSetID() {return pref.getString("game_set_id", null);}

}
