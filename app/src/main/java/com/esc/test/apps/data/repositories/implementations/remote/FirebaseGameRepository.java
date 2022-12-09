package com.esc.test.apps.data.repositories.implementations.remote;

import static com.esc.test.apps.common.utils.DatabaseConstants.ACTIVE_GAME;
import static com.esc.test.apps.common.utils.DatabaseConstants.DISPLAY_NAME;
import static com.esc.test.apps.common.utils.DatabaseConstants.FRIENDS;
import static com.esc.test.apps.common.utils.DatabaseConstants.FRIEND_INVITE;
import static com.esc.test.apps.common.utils.DatabaseConstants.FRIEND_REQUEST;
import static com.esc.test.apps.common.utils.DatabaseConstants.GAMES;
import static com.esc.test.apps.common.utils.DatabaseConstants.GAME_REQUEST;
import static com.esc.test.apps.common.utils.DatabaseConstants.MOVES;
import static com.esc.test.apps.common.utils.DatabaseConstants.STARTER;
import static com.esc.test.apps.common.utils.DatabaseConstants.USERS;
import static com.esc.test.apps.common.utils.DatabaseConstants.WINNER;
import static com.esc.test.apps.common.utils.Utils.dispose;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.esc.test.apps.R;
import com.esc.test.apps.data.persistence.GamePreferences;
import com.esc.test.apps.data.persistence.UserPreferences;
import com.esc.test.apps.data.models.pojos.UserInfo;
import com.esc.test.apps.data.repositories.FbGameRepo;
import com.esc.test.apps.data.repositories.FbMoveRepo;
import com.esc.test.apps.data.source.remote.FirebaseQueryLiveData;
import com.esc.test.apps.data.source.remote.FirebaseQuerySingleData;
import com.esc.test.apps.common.utils.SingleLiveEvent;
import com.esc.test.apps.common.utils.Utils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

@Singleton
public class FirebaseGameRepository implements FbGameRepo {

    private final GamePreferences gamePref;
    private final Application app;
    private final DatabaseReference gamesRef;
    private final DatabaseReference usersRef;
    private final SingleLiveEvent<UserInfo> newFriend = new SingleLiveEvent<>();
    private final SingleLiveEvent<String[]> startGame = new SingleLiveEvent<>();
    private final SingleLiveEvent<Boolean> quit = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> error = new SingleLiveEvent<>();
    private final PublishSubject<String> gameId = PublishSubject.create();
    private FirebaseQuerySingleData gameActive;
    private final FbMoveRepo fbMoveRepo;
    private final Random rand;
    private Disposable d;
    private String gameSetID;
    private String friendUID;
    private String uid;
    private ValueEventListener playerUIDsListener;
    public static final String TAG = "[Firebase]";

    @Inject
    public FirebaseGameRepository (Application app, Random rand, GamePreferences gamePref,
                                   DatabaseReference db, FbMoveRepo fbMoveRepo, UserPreferences userPref
    ) {
        this.app = app;
        this.gamePref = gamePref;
        this.fbMoveRepo = fbMoveRepo;
        this.rand = rand;
        gamesRef = db.child(GAMES);
        usersRef = db.child(USERS);
        d = userPref.getUserPreference().subscribeOn(AndroidSchedulers.mainThread()).doOnNext(prefs -> {
            uid = prefs.getUid();
            dispose(d);
        }).subscribe();
    }

    @Override
    public void findFriend(@NonNull String friend_name) {
        Query findFriend = usersRef.orderByChild(DISPLAY_NAME).equalTo(friend_name);
        findFriend.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Log.d(TAG, "onChildAdded: " + snapshot.getValue(UserInfo.class));
                    UserInfo userInfo = snapshot.getValue(UserInfo.class);
                    userInfo.setUid(snapshot.getKey());
                    newFriend.postValue(userInfo);
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

    @Override
    public void acceptInvite(UserInfo user) {
        usersRef.child(uid).child(FRIENDS).child(user.getUid()).child(DISPLAY_NAME).setValue(user.getDisplay_name());
    }

    @Override
    public void startGame(UserInfo user, boolean firstPlayer) {
        String gameSetRef = Utils.getGameSetUID(uid, user.getUid(), 0);
        String gameRef = Utils.getGameUID();
        gamePref.updateGameIdJava(gameRef);
        Log.d(TAG, "startGame: " + gameRef);
        if (firstPlayer) {
            String startPlayer = gameSetup(user.getUid(), gameSetRef, gameRef, uid);
                if (startPlayer.equals(uid))
                    startInfo(gameSetRef, app.getString(R.string.circle));
                else startInfo(gameSetRef, app.getString(R.string.cross));
        } else startInfo(gameSetRef, friendStart(user.getStarter()));
    }

    private String gameSetup(String guestUid, String gameSetRef, String gameRef, String uid) {
        String startPlayer = rand.nextBoolean() ? guestUid : uid;
        Log.d(TAG, "gameSetup: ");
        gamesRef.child(gameSetRef).child(gameRef).child(STARTER).setValue(startPlayer)
                .addOnCompleteListener(task -> Log.d(TAG, "gameSetup: game details sent"));
        return startPlayer;
    }

    private void startInfo(String gameSetRef, String friendGamePiece) {
        startGame.postValue(new String[] {gameSetRef, friendGamePiece});
    }

    private String friendStart(boolean friendStart) {
        return friendStart ? app.getString(R.string.cross) : app.getString(R.string.circle);
    }

    @Override
    public void sendGameInvite(UserInfo user, boolean startGame) {
        usersRef.child(user.getUid()).child(FRIENDS).child(uid)
                .child(GAME_REQUEST).setValue(startGame);
    }

    @Override
    public void inviteNewFriend() {
        UserInfo userInfo = newFriend.getValue();
        Log.d(TAG, "sendInvite: " + userInfo.getUid());
        usersRef.child(uid).child(FRIEND_INVITE).child(userInfo.getUid())
                .child(DISPLAY_NAME).setValue(userInfo.getDisplay_name());
    }

    @Override
    public void endGame(String winner) {
        final String winPlayer = winner == null ? friendUID : uid;
        d = gamePref.getGamePreference().subscribeOn(Schedulers.io()).doOnNext( pref -> {
            gamesRef.child(gameSetID).child(pref.getGameId()).child(WINNER).setValue(winPlayer)
                .addOnCompleteListener( task -> quit.postValue(true))
                .addOnFailureListener( task -> error.postValue(app.getString(R.string.quit_error)));
            dispose(d);
        }).subscribe();
    }

    @Override
    public void getGameUID(@NonNull String uids) {
        setEventListener();
        gameSetID = uids;
        gamePref.updateGameSetIdJava(uids);
        String[] players = uids.split("_");
        if (players[0].equals(uid)) {
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
                    String gameId = snapshot.getValue().toString();
                    setGameActiveState(gameId);
                    DatabaseReference movesRef = gamesRef.child(gameSetID).child(gameId).child(MOVES);
                    fbMoveRepo.checkCurrentGameMoves(movesRef);
                    gamePref.updateGameIdJava(gameId);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: cannot find your friends");
            }
        };
    }

    @Override
    public LiveData<List<UserInfo>> getActiveFriends(String uid) {
        FirebaseQueryLiveData friends = new FirebaseQueryLiveData(usersRef.child(uid).child(FRIENDS));
        return Transformations.map(friends, this::getFriends);
    }

    @Override
    public LiveData<List<UserInfo>> getFriendRequests(String uid) {
        FirebaseQueryLiveData requests = new FirebaseQueryLiveData(usersRef.child(uid).child(FRIEND_REQUEST));
        return Transformations.map(requests, this::getFriends);
    }

    @Override
    public void setGameActiveState(@NonNull String gameId) {
        gameActive = new FirebaseQuerySingleData(gamesRef.child(gameSetID).child(gameId).child(WINNER));
        this.gameId.onNext(gameId);
        this.gameId.onComplete();
    }

    @Override
    public LiveData<Map<String, String>> getGameActiveState() {
        return Transformations.map(gameActive, snapshot -> {
            String winner = snapshot.getValue(String.class);
            if (winner == null) winner = "";
            String player = !Objects.equals(winner, uid) ? "Your friend" : "You";
            return Map.of("winner", winner, "player", player);
        });
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

    @NonNull
    @Override
    public SingleLiveEvent<UserInfo> getNewFriend() {
        return newFriend;
    }

    @NonNull
    @Override
    public SingleLiveEvent<String[]> getStartGame() {
        return startGame;
    }

    @NonNull
    @Override
    public final PublishSubject<String> getGameId() { return gameId; }

    @NonNull
    @Override
    public SingleLiveEvent<Boolean> getQuit() {
        return null;
    }

    @NonNull
    @Override
    public SingleLiveEvent<String> getError() {
        return null;
    }
}
