package com.esc.test.apps.pojos;

import com.google.gson.annotations.SerializedName;

public class UserInfo {

    @SerializedName("game_invite")
    public boolean game_invite;
    @SerializedName("display_name")
    private String display_name;
    @SerializedName("token")
    private String token;
    @SerializedName("uid")
    private String uid;
    @SerializedName("profile_picture")
    private String profilePicture;
    @SerializedName("status")
    private String status;
    @SerializedName("game_request")
    private boolean game_request;
    @SerializedName("starter")
    private String starter;
    @SerializedName("active_game")
    private String active_game;

    public UserInfo() {}

    public String getDisplay_name() {return display_name;}
    public void setDisplay_name(String display_name) { this.display_name = display_name;}

    public String getToken() {return token;}
    public void setToken(String token) {this.token = token;}

    public String getUid() {return uid;}
    public void setUid(String uid) {this.uid = uid;}

    public String getProfilePicture() {return profilePicture;}
    public void setProfilePicture(String profilePicture) {this.profilePicture = profilePicture;}

    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}

    public boolean getGame_request() {return game_request;}
    public void setGame_request(boolean game_request) {this.game_request = game_request;}

    public boolean getGame_invite() {return  game_invite;}
    public void setGame_invite(boolean game_invite) {this.game_invite = game_invite;}

    public String getStarter() {return starter;}
    public void setStarter(String starter) {this.starter = starter;}

    public String getActive_game() {return active_game;}
    public void setActive_game(String active_game) {this.active_game = active_game;}
}
