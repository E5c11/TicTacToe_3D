package com.esc.test.apps.board.games.data;

public class Turn {

    private final String turn;
    private final boolean friendsTurn;

    public Turn(String turn, boolean friendsTurn) {
        this.turn = turn;
        this.friendsTurn = friendsTurn;
    }

    public String getTurn() { return turn; }

    public boolean isFriendsTurn() { return friendsTurn; }
}
