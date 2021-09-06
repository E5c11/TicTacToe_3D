package com.esc.test.apps.modelviews;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.R;
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

import com.esc.test.apps.datastore.GameState;

import com.esc.test.apps.network.FirebaseQueryLiveData;
import com.esc.test.apps.other.ResourceProvider;
import dagger.hilt.android.lifecycle.HiltViewModel;
import com.esc.test.apps.datastore.UserDetails;
import com.esc.test.apps.pojos.UserInfo;

@HiltViewModel
public class FriendsModelView extends ViewModel {

    private final DatabaseReference games;
    private final DatabaseReference users;
    private final Application app;
    private final UserDetails userDetails;
    private final GameState gameState;
    private final MutableLiveData<UserInfo> newFriend = new MutableLiveData<>();
    private final MutableLiveData<String[]> startGame = new MutableLiveData<>();
    public static final String lookUp = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final String TAG = "myT";

    @Inject
    public FriendsModelView(GameState gameState, Application app,
                            DatabaseReference db, UserDetails userDetails
    ) {
        this.app = app;
        this.userDetails = userDetails;
        this.gameState = gameState;
        games = db.child(app.getString(R.string.games));
        users = db.child(app.getString(R.string.users));
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

    public void acceptInvite(UserInfo user) {
        users.child(user.getUid()).child(app.getString(R.string.friends)).child(userDetails.getUid())
                .child(app.getString(R.string.display_name)).setValue(userDetails.getDisplayName());
        users.child(userDetails.getUid()).child(app.getString(R.string.friends)).child(user.getUid())
                .child(app.getString(R.string.display_name)).setValue(user.getDisplay_name());
        users.child(user.getUid()).child(app.getString(R.string.friend_invites)).child(userDetails.getUid()).removeValue();
        users.child(userDetails.getUid()).child(app.getString(R.string.friend_requests)).child(user.getUid()).removeValue();
    }

    public void startGame(UserInfo user, boolean firstPlayer) {
        String gameSetRef = getGameSetUID(userDetails.getUid(), user.getUid());
        String startPlayer;
        if (firstPlayer) {
            String gameRef = getGameUID();
            gameState.setGameID(gameRef);
            users.child(userDetails.getUid()).child(app.getString(R.string.friends)).child(user.getUid()).child(app.getString(R.string.active_game)).setValue(gameRef);
            users.child(user.getUid()).child(app.getString(R.string.friends)).child(userDetails.getUid()).child(app.getString(R.string.active_game)).setValue(gameRef);
            games.child(gameSetRef).child(gameRef).child(app.getString(R.string.game_active)).setValue(true);
            startPlayer = gameSetup(user.getUid(), gameSetRef, gameRef);
            if (startPlayer.equals(userDetails.getUid())) startGame.setValue(new String[] {gameSetRef, app.getString(R.string.circle)});
            else startGame.setValue(new String[] {gameSetRef, app.getString(R.string.cross)});
        } else startGame.setValue(new String[] {gameSetRef, user.getStarter()});

        changeInviteState(user.getUid());
    }

    public void changeInviteState(String uid) {
        users.child(userDetails.getUid()).child(app.getString(R.string.friends)).child(uid).child(app.getString(R.string.game_request)).setValue(false);
        users.child(uid).child(app.getString(R.string.friends)).child(userDetails.getUid()).child(app.getString(R.string.game_invite)).setValue(false);
    }

    public void sendGameInvite(UserInfo user, boolean startGame) {
        users.child(user.getUid()).child(app.getString(R.string.friends)).child(userDetails.getUid()).child(app.getString(R.string.game_request)).setValue(startGame);
        receiveGameInvite(user.getUid(), startGame);
    }
    private void receiveGameInvite(String uid, boolean startGame) {
        users.child(userDetails.getUid()).child(app.getString(R.string.friends)).child(uid).child(app.getString(R.string.game_invite)).setValue(startGame);
    }

    public LiveData<List<UserInfo>> getActiveFriends() {
        FirebaseQueryLiveData friends = new FirebaseQueryLiveData(users.child(userDetails.getUid()).child(app.getString(R.string.friends)));
        return Transformations.map(friends, this::getFriends);
    }

    public LiveData<List<UserInfo>> getFriendRequests() {
        FirebaseQueryLiveData requests = new FirebaseQueryLiveData(users.child(userDetails.getUid()).child(app.getString(R.string.friend_requests)));
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

    private String gameSetup(String uid, String gameSetRef, String gameRef) {
        String startPlayer = new Random().nextBoolean() ? uid : userDetails.getUid();
        Log.d(TAG, "start player: " + startPlayer);
        games.child(gameSetRef).child(gameRef).child(app.getString(R.string.starter)).setValue(startPlayer);
        notifySecondPlayer(startPlayer, uid);
        return startPlayer;
    }

    private void notifySecondPlayer(String startPlayer, String opponent ) {
        if (startPlayer.equals(userDetails.getUid())) {
            Log.d(TAG, "notifySecondPlayer: I start");
            users.child(userDetails.getUid()).child(app.getString(R.string.friends)).child(opponent).child(app.getString(R.string.starter)).setValue(app.getString(R.string.me));
            users.child(opponent).child(app.getString(R.string.friends)).child(userDetails.getUid()).child(app.getString(R.string.starter)).setValue(app.getString(R.string.opponent));
        } else {
            Log.d(TAG, "notifySecondPlayer: I don't start");
            users.child(userDetails.getUid()).child(app.getString(R.string.friends)).child(opponent).child(app.getString(R.string.starter)).setValue(app.getString(R.string.opponent));
            users.child(opponent).child(app.getString(R.string.friends)).child(userDetails.getUid()).child(app.getString(R.string.starter)).setValue(app.getString(R.string.me));
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

    private int getLookUpIndex(char firstChar) {
        return lookUp.indexOf(firstChar);
    }

    public void inviteNewFriend() {
        UserInfo userInfo = getNewFriend().getValue();
//        Log.d("myT", "invite friend " + userInfo.getUid());
        users.child(userDetails.getUid()).child(app.getString(R.string.friend_invites))
                .child(userInfo.getUid()).child(app.getString(R.string.invite_time)).setValue(getDate());
        users.child(userDetails.getUid()).child(app.getString(R.string.friend_invites))
                .child(userInfo.getUid()).child(app.getString(R.string.display_name)).setValue(userInfo.getDisplay_name());
        sendRequest(userInfo);
    }
    private void sendRequest(UserInfo userInfo) {
        users.child(userInfo.getUid()).child(app.getString(R.string.friend_requests))
                .child(userDetails.getUid()).child(app.getString(R.string.request_time)).setValue(getDate());
        //Log.d("myT", "name is: " + userDetails.getDisplayName());
        users.child(userInfo.getUid()).child(app.getString(R.string.friend_requests))
                .child(userDetails.getUid()).child(app.getString(R.string.display_name)).setValue(userDetails.getDisplayName());
        //Log.d(TAG, "sendRequest: " + userDetails.getDisplayName());
    }

    private String getDaysAgo(LocalDate pastDate) {
        LocalDate timeNow = LocalDate.now();
        return String.valueOf(ChronoUnit.DAYS.between(pastDate, timeNow));
    }

    private String getDate() {return LocalDate.now().toString();}

    public MutableLiveData<UserInfo> getNewFriend() {return newFriend;}

    public MutableLiveData<String[]> getStartGame() {return startGame;}
}
