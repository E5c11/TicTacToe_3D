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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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
    private String friendGamePiece;
    private ValueEventListener playerUIDsListener;
    public static final String lookUp = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final String TAG = "myT";
    private static final String USERS = "users";
    private static final String DISPLAY_NAME = "display_name";
    private static final String GAMES = "games";
    private static final String MOVES = "moves";
    private static final String FRIENDS = "friends";
    private static final String ACTIVE_GAME = "active_game";
    private static final String STARTER = "starter";
    private static final String GAME_INVITE = "game_invite";
    private static final String GAME_REQUEST = "game_request";
    private static final String FRIEND_INVITE = "friend_invites";
    private static final String FRIEND_REQUEST = "friend_requests";
    private static final String INVITE_TIME = "invite_time";
    private static final String REQUEST_TIME = "request_time";
    private static final String WINNER = "winner";
    private static final String ME = "me";
    private static final String OPPONENT = "opponent";

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
        Query findFriend = usersRef.orderByChild(app.getString(R.string.display_name)).equalTo(friend_name);
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
        usersRef.child(user.getUid()).child(FRIENDS).child(userDetails.getUid())
                .child(DISPLAY_NAME).setValue(userDetails.getDisplayName());
        usersRef.child(userDetails.getUid()).child(FRIENDS).child(user.getUid())
                .child(DISPLAY_NAME).setValue(user.getDisplay_name());
        usersRef.child(user.getUid()).child(FRIEND_INVITE).child(userDetails.getUid()).removeValue();
        usersRef.child(userDetails.getUid()).child(FRIEND_REQUEST).child(user.getUid()).removeValue();
    }

    public void startGame(UserInfo user, boolean firstPlayer) {
        String gameSetRef = getGameSetUID(userDetails.getUid(), user.getUid(), 0);
        String startPlayer;
        if (firstPlayer) {
            String gameRef = getGameUID();
            gameState.setGameID(gameRef);
            usersRef.child(userDetails.getUid()).child(FRIENDS).child(user.getUid()).child(ACTIVE_GAME).setValue(gameRef);
            usersRef.child(user.getUid()).child(FRIENDS).child(userDetails.getUid()).child(ACTIVE_GAME).setValue(gameRef);
            gamesRef.child(gameSetRef).child(gameRef).child(ACTIVE_GAME).setValue(true);
            startPlayer = gameSetup(user.getUid(), gameSetRef, gameRef);
            if (startPlayer.equals(userDetails.getUid())) startGame.setValue(new String[] {gameSetRef, app.getString(R.string.circle)});
            else startGame.setValue(new String[] {gameSetRef, app.getString(R.string.cross)});
        } else startGame.setValue(new String[] {gameSetRef, user.getStarter()});

        changeInviteState(user.getUid());
    }
    public SingleLiveEvent<String[]> getStartGame() { return startGame; }

    public void changeInviteState(String uid) {
        usersRef.child(userDetails.getUid()).child(FRIENDS).child(uid).child(GAME_REQUEST).setValue(false);
        usersRef.child(uid).child(FRIENDS).child(userDetails.getUid()).child(GAME_INVITE).setValue(false);
    }

    public void sendGameInvite(UserInfo user, boolean startGame) {
        usersRef.child(user.getUid()).child(FRIENDS).child(userDetails.getUid()).child(GAME_REQUEST).setValue(startGame);
        receiveGameInvite(user.getUid(), startGame);
    }
    private void receiveGameInvite(String uid, boolean startGame) {
        usersRef.child(userDetails.getUid()).child(FRIENDS).child(uid).child(GAME_INVITE).setValue(startGame);
    }

    private String gameSetup(String uid, String gameSetRef, String gameRef) {
        String startPlayer = new Random().nextBoolean() ? uid : userDetails.getUid();
        Log.d(TAG, "start player: " + startPlayer);
        gamesRef.child(gameSetRef).child(gameRef).child(STARTER).setValue(startPlayer);
        notifySecondPlayer(startPlayer, uid);
        return startPlayer;
    }

    private void notifySecondPlayer(String startPlayer, String opponent ) {
        if (startPlayer.equals(userDetails.getUid())) {
            Log.d(TAG, "notifySecondPlayer: I start");
            usersRef.child(userDetails.getUid()).child(FRIENDS).child(opponent).child(STARTER).setValue(ME);
            usersRef.child(opponent).child(FRIENDS).child(userDetails.getUid()).child(STARTER).setValue(OPPONENT);
        } else {
            Log.d(TAG, "notifySecondPlayer: I don't start");
            usersRef.child(userDetails.getUid()).child(FRIENDS).child(opponent).child(STARTER).setValue(OPPONENT);
            usersRef.child(opponent).child(FRIENDS).child(userDetails.getUid()).child(STARTER).setValue(ME);
        }
        //Log.d(TAG, "notifySecondPlayer: set friend start " + gameState.getFriendStart());
    }

    private String getGameUID() {
        return Long.toString(System.currentTimeMillis());
    }

    private String getGameSetUID(String one, String two, int... i) {
        int index = i[0];
        if (Integer.compare(getLookUpIndex(Character.toUpperCase(one.charAt(index))), getLookUpIndex(Character.toUpperCase(two.charAt(index)))) == -1)
            return one + "_" + two;
        else if(Integer.compare(getLookUpIndex(Character.toUpperCase(one.charAt(index))), getLookUpIndex(Character.toUpperCase(two.charAt(index)))) == 1)
            return two + "_" + one;
        else return getGameSetUID(one, two, index++);
    }

    private int getLookUpIndex(char firstChar) { return lookUp.indexOf(firstChar); }

    public void inviteNewFriend() {
        UserInfo userInfo = getNewFriend().getValue();
//        Log.d("myT", "invite friend " + userInfo.getUid());
        usersRef.child(userDetails.getUid()).child(FRIEND_INVITE)
                .child(userInfo.getUid()).child(INVITE_TIME).setValue(getDate());
        usersRef.child(userDetails.getUid()).child(FRIEND_INVITE)
                .child(userInfo.getUid()).child(DISPLAY_NAME).setValue(userInfo.getDisplay_name());
        sendRequest(userInfo);
    }
    private void sendRequest(UserInfo userInfo) {
        usersRef.child(userInfo.getUid()).child(FRIEND_REQUEST)
                .child(userDetails.getUid()).child(REQUEST_TIME).setValue(getDate());
        usersRef.child(userInfo.getUid()).child(FRIEND_REQUEST)
                .child(userDetails.getUid()).child(DISPLAY_NAME).setValue(userDetails.getDisplayName());
    }

    private String getDate() {return LocalDate.now().toString();}

    private String getDaysAgo(LocalDate pastDate) {
        LocalDate timeNow = LocalDate.now();
        return String.valueOf(ChronoUnit.DAYS.between(pastDate, timeNow));
    }

    public void endGame(String winner) {
        if (winner == null) winner = friendUID;
        gamesRef.child(gameSetID).child(gameState.getGameID()).child(WINNER).setValue(winner);
    }

    public void getGameUID(String uids, String friendPiece) {
        setEventListener();
        friendGamePiece = friendPiece;
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
                    fbMoveRepo.checkCurrentGameMoves(movesRef, friendGamePiece);
                    gameState.setGameID(ref);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
