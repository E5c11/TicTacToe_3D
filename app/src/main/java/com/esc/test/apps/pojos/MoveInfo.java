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

    public MoveInfo(String coordinates, String position, String piece_played, String moveId, String uid) {
        this.piece_played = piece_played;
        this.coordinates = coordinates;
        this.position = position;
        this.moveID = moveId;
        this.uid = uid;
    }

    public MoveInfo() {}

    public String getPiece_played() {return piece_played;}

    public String getCoordinates() {return coordinates;}

    public String getPosition() {return position;}

    public String getMoveID() {return moveID;}

    public String getUid() { return uid; }

    public void setPiece_played(String piece_played) {
        this.piece_played = piece_played;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setMoveID(String moveID) { this.moveID = moveID; }

    public void setUid(String uid) { this.uid = uid; }
}
