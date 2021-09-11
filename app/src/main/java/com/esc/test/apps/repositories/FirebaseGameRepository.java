package com.esc.test.apps.repositories;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.esc.test.apps.R;
import com.esc.test.apps.datastore.GameState;
import com.esc.test.apps.datastore.UserDetails;
import com.esc.test.apps.network.FirebaseQueryLiveData;
import com.esc.test.apps.utils.SingleLiveEvent;
import com.esc.test.apps.pojos.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseGameRepository {

    private final GameState gameState;
    private final Application app;
    private final DatabaseReference games;
    private final DatabaseReference users;
    private final UserDetails userDetails;
    private final SingleLiveEvent<UserInfo> newFriend = new SingleLiveEvent<>();
    private final SingleLiveEvent<String[]> startGame = new SingleLiveEvent<>();
    private final FirebaseQueryLiveData friends;
    private final FirebaseQueryLiveData requests;
    public static final String lookUp = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final String TAG = "myT";
    private static final String USERS = "users";
    private static final String DISPLAY_NAME = "display_name";
    private static final String GAMES = "games";
    private static final String FRIENDS = "friends";
    private static final String ACTIVE_GAME = "active_game";
    private static final String STARTER = "starter";
    private static final String GAME_ACTIVE = "game_active";
    private static final String GAME_INVITE = "game_invite";
    private static final String GAME_REQUEST = "game_request";
    private static final String FRIEND_INVITE = "friend_invites";
    private static final String FRIEND_REQUEST = "friend_requests";
    private static final String INVITE_TIME = "invite_time";
    private static final String REQUEST_TIME = "request_time";
    private static final String ME = "me";
    private static final String OPPONENT = "opponent";

    @Inject
    public FirebaseGameRepository (GameState gameState, Application app,
                                   DatabaseReference db, UserDetails userDetails
    ) {
        this.gameState = gameState;
        this.app = app;
        this.userDetails = userDetails;
        games = db.child(GAMES);
        users = db.child(USERS);
        friends = new FirebaseQueryLiveData(users.child(userDetails.getUid()).child(FRIENDS));
        requests = new FirebaseQueryLiveData(users.child(userDetails.getUid()).child(FRIEND_REQUEST));
    }

    public void findFriend(String friend_name) {
        Query findFriend = users.orderByChild(app.getString(R.string.display_name)).equalTo(friend_name);
        Log.d(TAG, "findFriend: ");
        findFriend.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Log.d(TAG, "onChildAdded: " + snapshot.getValue(UserInfo.class));
                    UserInfo userInfo = snapshot.getValue(UserInfo.class);
                    userInfo.setUid(snapshot.getKey());
                    newFriend.setValue(userInfo);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    public SingleLiveEvent<UserInfo> getNewFriend() { return newFriend; }

    public void acceptInvite(UserInfo user) {
        users.child(user.getUid()).child(FRIENDS).child(userDetails.getUid())
                .child(DISPLAY_NAME).setValue(userDetails.getDisplayName());
        users.child(userDetails.getUid()).child(FRIENDS).child(user.getUid())
                .child(DISPLAY_NAME).setValue(user.getDisplay_name());
        users.child(user.getUid()).child(FRIEND_INVITE).child(userDetails.getUid()).removeValue();
        users.child(userDetails.getUid()).child(FRIEND_REQUEST).child(user.getUid()).removeValue();
    }

    public void startGame(UserInfo user, boolean firstPlayer) {
        String gameSetRef = getGameSetUID(userDetails.getUid(), user.getUid());
        String startPlayer;
        if (firstPlayer) {
            String gameRef = getGameUID();
            gameState.setGameID(gameRef);
            users.child(userDetails.getUid()).child(FRIENDS).child(user.getUid()).child(ACTIVE_GAME).setValue(gameRef);
            users.child(user.getUid()).child(FRIENDS).child(userDetails.getUid()).child(ACTIVE_GAME).setValue(gameRef);
            games.child(gameSetRef).child(gameRef).child(GAME_ACTIVE).setValue(true);
            startPlayer = gameSetup(user.getUid(), gameSetRef, gameRef);
            if (startPlayer.equals(userDetails.getUid())) startGame.setValue(new String[] {gameSetRef, app.getString(R.string.circle)});
            else startGame.setValue(new String[] {gameSetRef, app.getString(R.string.cross)});
        } else startGame.setValue(new String[] {gameSetRef, user.getStarter()});

        changeInviteState(user.getUid());
    }
    public SingleLiveEvent<String[]> getStartGame() { return startGame; }

    public void changeInviteState(String uid) {
        users.child(userDetails.getUid()).child(FRIENDS).child(uid).child(GAME_REQUEST).setValue(false);
        users.child(uid).child(FRIENDS).child(userDetails.getUid()).child(GAME_INVITE).setValue(false);
    }

    public void sendGameInvite(UserInfo user, boolean startGame) {
        users.child(user.getUid()).child(FRIENDS).child(userDetails.getUid()).child(GAME_REQUEST).setValue(startGame);
        receiveGameInvite(user.getUid(), startGame);
    }
    private void receiveGameInvite(String uid, boolean startGame) {
        users.child(userDetails.getUid()).child(FRIENDS).child(uid).child(GAME_INVITE).setValue(startGame);
    }

    private String gameSetup(String uid, String gameSetRef, String gameRef) {
        String startPlayer = new Random().nextBoolean() ? uid : userDetails.getUid();
        Log.d(TAG, "start player: " + startPlayer);
        games.child(gameSetRef).child(gameRef).child(STARTER).setValue(startPlayer);
        notifySecondPlayer(startPlayer, uid);
        return startPlayer;
    }

    private void notifySecondPlayer(String startPlayer, String opponent ) {
        if (startPlayer.equals(userDetails.getUid())) {
            Log.d(TAG, "notifySecondPlayer: I start");
            users.child(userDetails.getUid()).child(FRIENDS).child(opponent).child(STARTER).setValue(ME);
            users.child(opponent).child(FRIENDS).child(userDetails.getUid()).child(STARTER).setValue(OPPONENT);
        } else {
            Log.d(TAG, "notifySecondPlayer: I don't start");
            users.child(userDetails.getUid()).child(FRIENDS).child(opponent).child(STARTER).setValue(OPPONENT);
            users.child(opponent).child(FRIENDS).child(userDetails.getUid()).child(STARTER).setValue(ME);
        }
        //Log.d(TAG, "notifySecondPlayer: set friend start " + gameState.getFriendStart());
    }

    private String getGameUID() {
        return Long.toString(System.currentTimeMillis());
    }

    private String getGameSetUID(String one, String two) {
        if (Integer.compare(getLookUpIndex(Character.toUpperCase(one.charAt(0))), getLookUpIndex(Character.toUpperCase(two.charAt(0)))) == -1)
            return one + "_" + two;
        else if(Integer.compare(getLookUpIndex(Character.toUpperCase(one.charAt(0))), getLookUpIndex(Character.toUpperCase(two.charAt(0)))) == 1)
            return two + "_" + one;
        else return getGameSetUID(one, two);
    }

    private int getLookUpIndex(char firstChar) { return lookUp.indexOf(firstChar); }

    public void inviteNewFriend() {
        UserInfo userInfo = getNewFriend().getValue();
//        Log.d("myT", "invite friend " + userInfo.getUid());
        users.child(userDetails.getUid()).child(FRIEND_INVITE)
                .child(userInfo.getUid()).child(INVITE_TIME).setValue(getDate());
        users.child(userDetails.getUid()).child(FRIEND_INVITE)
                .child(userInfo.getUid()).child(DISPLAY_NAME).setValue(userInfo.getDisplay_name());
        sendRequest(userInfo);
    }
    private void sendRequest(UserInfo userInfo) {
        users.child(userInfo.getUid()).child(FRIEND_REQUEST)
                .child(userDetails.getUid()).child(REQUEST_TIME).setValue(getDate());
        users.child(userInfo.getUid()).child(FRIEND_REQUEST)
                .child(userDetails.getUid()).child(DISPLAY_NAME).setValue(userDetails.getDisplayName());
    }

    private String getDate() {return LocalDate.now().toString();}

    private String getDaysAgo(LocalDate pastDate) {
        LocalDate timeNow = LocalDate.now();
        return String.valueOf(ChronoUnit.DAYS.between(pastDate, timeNow));
    }

    public LiveData<List<UserInfo>> getActiveFriends() {
        return Transformations.map(friends, this::getFriends);
    }

    public LiveData<List<UserInfo>> getFriendRequests() {
        return Transformations.map(requests, this::getFriends);
    }

    private List<UserInfo> getFriends(DataSnapshot dataSnapshot) {
        List<UserInfo> friendsList = new ArrayList<>();
        UserInfo user;
        Log.d(TAG, "Active friend downloaded: " + dataSnapshot.getChildrenCount());
        for(DataSnapshot snap : dataSnapshot.getChildren()){
            user = snap.getValue(UserInfo.class);
            user.setUid(snap.getKey());
            friendsList.add(user);
        }
        return friendsList;
    }
}
