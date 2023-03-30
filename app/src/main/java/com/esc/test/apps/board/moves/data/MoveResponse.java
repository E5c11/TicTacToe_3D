package com.esc.test.apps.board.moves.data;

import com.google.gson.annotations.SerializedName;

public class MoveResponse {

    @SerializedName("piece_played")
    private String piecePlayed;
    @SerializedName("coordinates")
    private String coordinates;
    @SerializedName("position")
    private String position;
    @SerializedName("move_id")
    private String moveID;
    @SerializedName("uid")
    private String uid;

    public MoveResponse(String coordinates, String position, String piecePlayed, String moveId, String uid) {
        this.piecePlayed = piecePlayed;
        this.coordinates = coordinates;
        this.position = position;
        this.moveID = moveId;
        this.uid = uid;
    }

    public MoveResponse() {}

    public String getPiecePlayed() {return piecePlayed;}

    public String getCoordinates() {return coordinates;}

    public String getPosition() {return position;}

    public String getMoveID() {return moveID;}

    public String getUid() { return uid; }

    public void setPiecePlayed(String piecePlayed) {
        this.piecePlayed = piecePlayed;
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
