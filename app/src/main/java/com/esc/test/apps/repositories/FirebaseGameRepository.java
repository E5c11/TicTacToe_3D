package com.esc.test.apps.repositories;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.esc.test.apps.R;
import com.esc.test.apps.datastore.GameState;
import com.esc.test.apps.datastore.UserDetails;
import com.esc.test.apps.entities.Move;
import com.esc.test.apps.network.FirebaseQueryLiveData;
import com.esc.test.apps.pojos.MoveInfo;
import com.esc.test.apps.utils.SingleLiveEvent;
import com.esc.test.apps.pojos.UserInfo;
import com.esc.test.apps.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseGameRepository {

    private final GameState gameState;
    private final Application app;
    private final DatabaseReference gamesRef;
    private final DatabaseReference usersRef;
    private final UserDetails userDetails;
    private final SingleLiveEvent<UserInfo> newFriend = new SingleLiveEvent<>();
    private final SingleLiveEvent<String[]> startGame = new SingleLiveEvent<>();
    private final FirebaseQueryLiveData friends;
    private final FirebaseQueryLiveData requests;
    private final FirebaseMoveRepository fbMoveRepo;
    private String gameSetID;
    private String friendUID;
    private ValueEventListener playerUIDsListener;
    public static final String TAG = "myT";
    private static final String USERS = "users";
    private static final String DISPLAY_NAME = "display_name";
    private static final String GAMES = "games";
    private static final String MOVES = "moves";
    private static final String FRIENDS = "friends";
    private static final String ACTIVE_GAME = "active_game";
    private static final String STARTER = "starter";
    private static final String GAME_REQUEST = "game_request";
    private static final String FRIEND_INVITE = "friend_invites";
    private static final String FRIEND_REQUEST = "friend_requests";
    private static final String WINNER = "winner";

    @Inject
    public FirebaseGameRepository (GameState gameState, Application app,
                                   DatabaseReference db, UserDetails userDetails,
                                   FirebaseMoveRepository fbMoveRepo
    ) {
        this.gameState = gameState;
        this.app = app;
        this.userDetails = userDetails;
        this.fbMoveRepo = fbMoveRepo;
        gamesRef = db.child(GAMES);
        usersRef = db.child(USERS);
        friends = new FirebaseQueryLiveData(usersRef.child(userDetails.getUid()).child(FRIENDS));
        requests = new FirebaseQueryLiveData(usersRef.child(userDetails.getUid()).child(FRIEND_REQUEST));
    }

    public void findFriend(String friend_name) {
        Query findFriend = usersRef.orderByChild(DISPLAY_NAME).equalTo(friend_name);
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
        usersRef.child(userDetails.getUid()).child(FRIENDS).child(user.getUid()).child(DISPLAY_NAME)
                .setValue(user.getDisplay_name());
    }

    public void startGame(UserInfo user, boolean firstPlayer) {
        String gameSetRef = Utils.getGameSetUID(userDetails.getUid(), user.getUid(), 0);
        String gameRef = Utils.getGameUID();
        gameState.setGameID(gameRef);
        Log.d(TAG, "startGame: " + gameRef);
        if (firstPlayer) {
            String startPlayer = gameSetup(user.getUid(), gameSetRef, gameRef);
            if (startPlayer.equals(userDetails.getUid()))
                startInfo(gameSetRef, app.getString(R.string.circle));
            else startInfo(gameSetRef, app.getString(R.string.cross));
        } else startInfo(gameSetRef, friendStart(user.getStarter()));
    }

    private String gameSetup(String uid, String gameSetRef, String gameRef) {
        String startPlayer = new Random().nextBoolean() ? uid : userDetails.getUid();
        gamesRef.child(gameSetRef).child(gameRef).child(STARTER).setValue(startPlayer);
        return startPlayer;
    }

    private void startInfo(String gameSetRef, String friendGamePiece) {
        startGame.setValue(new String[] {gameSetRef, friendGamePiece});
    }

    private String friendStart(boolean friendStart) {
        return friendStart ? app.getString(R.string.cross) : app.getString(R.string.circle);
    }

    public SingleLiveEvent<String[]> getStartGame() { return startGame; }

    public void sendGameInvite(UserInfo user, boolean startGame) {
        usersRef.child(user.getUid()).child(FRIENDS).child(userDetails.getUid())
                .child(GAME_REQUEST).setValue(startGame);
    }

    public void inviteNewFriend() {
        UserInfo userInfo = getNewFriend().getValue();
        usersRef.child(userDetails.getUid()).child(FRIEND_INVITE)
                .child(userInfo.getUid()).child(DISPLAY_NAME).setValue(userInfo.getDisplay_name());
    }

    public void endGame(String winner) {
        if (winner == null) winner = friendUID;
        else winner = userDetails.getUid();
        gamesRef.child(gameSetID).child(gameState.getGameID()).child(WINNER).setValue(winner);
    }

    public void getGameUID(String uids) {
        setEventListener();
        gameSetID = uids;
        gameState.setGameSetID(uids);
        String[] players = uids.split("_");
        if (players[0].equals(userDetails.getUid())) {
            usersRef.child(players[1]).child(FRIENDS).child(players[0])
                    .child(ACTIVE_GAME).addListenerForSingleValueEvent(playerUIDsListener);
            friendUID = players[1];
        }
        else {
            usersRef.child(players[0]).child(FRIENDS).child(players[1])
                    .child(ACTIVE_GAME).addListenerForSingleValueEvent(playerUIDsListener);
            friendUID = players[0];
        }
    }

    private void setEventListener() {
        playerUIDsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String ref = snapshot.getValue().toString();
                    DatabaseReference movesRef = gamesRef.child(gameSetID).child(ref).child(MOVES);
                    fbMoveRepo.checkCurrentGameMoves(movesRef);
                    gameState.setGameID(ref);
                    Log.d(TAG, "onDataChange: " + ref);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: cannot find your friends");
            }
        };
    }

    public LiveData<List<UserInfo>> getActiveFriends() {
        return Transformations.map(friends, this::getFriends);
    }

    public LiveData<List<UserInfo>> getFriendRequests() {
        return Transformations.map(requests, this::getFriends);
    }

    private List<UserInfo> getFriends(DataSnapshot dataSnapshot) {
        List<UserInfo> friendsList = new ArrayList<>();
        Log.d(TAG, "Active friend downloaded: " + dataSnapshot.getChildrenCount());
        for(DataSnapshot snap : dataSnapshot.getChildren()){
            UserInfo user = snap.getValue(UserInfo.class);
            if (user != null) {
                user.setUid(snap.getKey());
                friendsList.add(user);
            }
        }
        return friendsList;
    }
}
