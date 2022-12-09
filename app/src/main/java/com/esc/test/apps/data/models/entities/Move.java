package com.esc.test.apps.data.models.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "game_moves_table")
public class Move {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String coordinates;

    private String position;

    private String piece_played;

    public Move(String coordinates, String position, String piece_played) {
        this.coordinates = coordinates;
        this.position = position;
        this.piece_played = piece_played;
    }

    public Move() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getPosition() {
        return position;
    }

    public String getPiece_played() {
        return piece_played;
    }

    public void setPosition(String position) {this.position = position;}

    public void setCoordinates(String coordinates) {this.coordinates = coordinates;}

    public void setPiece_played(String piece_played) {this.piece_played = piece_played;}
}
