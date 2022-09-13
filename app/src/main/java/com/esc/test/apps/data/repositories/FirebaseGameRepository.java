package com.esc.test.apps.data.repositories;

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
import com.esc.test.apps.data.objects.pojos.UserInfo;
import com.esc.test.apps.data.source.remote.FirebaseQueryLiveData;
import com.esc.test.apps.data.source.remote.FirebaseQuerySingleData;
import com.esc.test.apps.common.utils.ExecutorFactory;
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
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

@Singleton
public class FirebaseGameRepository {

    private final GamePreferences gamePref;
    private final Application app;
    private final DatabaseReference gamesRef;
    private final DatabaseReference usersRef;
    public final SingleLiveEvent<UserInfo> newFriend = new SingleLiveEvent<>();
    public final SingleLiveEvent<String[]> startGame = new SingleLiveEvent<>();
    private final SingleLiveEvent<Boolean> _quit = new SingleLiveEvent<>();
    public final SingleLiveEvent<Boolean> quit = _quit;
    private final SingleLiveEvent<String> _error = new SingleLiveEvent<>();
    public final SingleLiveEvent<String> error = _error;
    private final PublishSubject<String> _gameId = PublishSubject.create();
    public final Flowable<String> gameId = _gameId.toFlowable(BackpressureStrategy.LATEST);
    private FirebaseQuerySingleData gameActive;
    private final FirebaseMoveRepository fbMoveRepo;
    private final ExecutorService executor = ExecutorFactory.getSingleExecutor();
    private final Random rand;
    private Disposable d;
    private String gameSetID;
    private String friendUID;
    private String uid;
    private ValueEventListener playerUIDsListener;
    public static final String TAG = "[Firebase]}";

    @Inject
    public FirebaseGameRepository (Application app, Random rand, GamePreferences gamePref,
                                   DatabaseReference db, FirebaseMoveRepository fbMoveRepo, UserPreferences userPref
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

    public void findFriend(String friend_name) {
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

    public void acceptInvite(UserInfo user) {
        usersRef.child(uid).child(FRIENDS).child(user.getUid()).child(DISPLAY_NAME).setValue(user.getDisplay_name());
    }

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

    public void sendGameInvite(UserInfo user, boolean startGame) {
        usersRef.child(user.getUid()).child(FRIENDS).child(uid)
                .child(GAME_REQUEST).setValue(startGame);
    }

    public void inviteNewFriend() {
        UserInfo userInfo = newFriend.getValue();
        Log.d(TAG, "sendInvite: " + userInfo.getUid());
        usersRef.child(uid).child(FRIEND_INVITE).child(userInfo.getUid())
                .child(DISPLAY_NAME).setValue(userInfo.getDisplay_name());
    }

    public void endGame(String winner) {
        final String winPlayer = winner == null ? friendUID : uid;
        d = gamePref.getGamePreference().subscribeOn(Schedulers.io()).doOnNext( pref -> {
            gamesRef.child(gameSetID).child(pref.getGameId()).child(WINNER).setValue(winPlayer)
                .addOnCompleteListener( task -> _quit.postValue(true))
                .addOnFailureListener( task -> _error.postValue(app.getString(R.string.quit_error)));
            dispose(d);
        }).subscribe();
    }

    public void getGameUID(String uids) {
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

    public LiveData<List<UserInfo>> getActiveFriends(String uid) {
        FirebaseQueryLiveData friends = new FirebaseQueryLiveData(usersRef.child(uid).child(FRIENDS));
        return Transformations.map(friends, this::getFriends);
    }

    public LiveData<List<UserInfo>> getFriendRequests(String uid) {
        FirebaseQueryLiveData requests = new FirebaseQueryLiveData(usersRef.child(uid).child(FRIEND_REQUEST));
        return Transformations.map(requests, this::getFriends);
    }

    public void setGameActiveState(String gameId) {
        gameActive = new FirebaseQuerySingleData(gamesRef.child(gameSetID).child(gameId).child(WINNER));
        _gameId.onNext(gameId);
        _gameId.onComplete();
    }

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
}
