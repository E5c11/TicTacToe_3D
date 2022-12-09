package com.esc.test.apps.data.models.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "games_table")
public class Game {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String winner = "in progress";

    private String turn = "cross";

    private String moves = "get move reference";

    private String opponent = "opponent";

    private String starter = "not started";

    public Game(String winner, String turn, String moves, String opponent, String starter) {
        this.winner = winner;
        this.turn = turn;
        this.moves = moves;
        this.opponent = opponent;
        this.starter = starter;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public void setMoves(String moves) {
        this.moves = moves;
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }

    public void setStarter(String starter) {
        this.starter = starter;
    }

    public String getWinner() {
        return winner;
    }

    public String getTurn() {
        return turn;
    }

    public String getMoves() {
        return moves;
    }

    public String getOpponent() {
        return opponent;
    }

    public String getStarter() {
        return starter;
    }
}
