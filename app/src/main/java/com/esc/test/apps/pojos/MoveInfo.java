package com.esc.test.apps.pojos;

import com.google.gson.annotations.SerializedName;

public class MoveInfo {

    @SerializedName("piece_played")
    private String piece_played;
    @SerializedName("coordinates")
    private String coordinates;
    @SerializedName("position")
    private String position;
    @SerializedName("move_id")
    private String moveID;
    @SerializedName("uid")
    private String uid;

    public MoveInfo(String piece_played, String coordinates, String position, int moveId, String uid) {
        this.piece_played = piece_played;
        this.coordinates = coordinates;
        this.position = position;
        //this.moveID = moveID;
        this.uid = uid;
    }

    public MoveInfo() {}

    public String getPiece_played() {return piece_played;}

    public String getCoordinates() {return coordinates;}

    public String getPosition() {return position;}

    //public String getMoveID() {return moveID;}

    public void setPiece_played(String piece_played) {
        this.piece_played = piece_played;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
