package com.esc.test.apps.data.pojos;

public class MoveUpdate {

    private String pos;
    private String piece;

    public MoveUpdate( String pos,String piece){
        this.pos = pos;
        this.piece = piece;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getPiece() {
        return piece;
    }

    public void setPiece(String piece) {
        this.piece = piece;
    }
}
