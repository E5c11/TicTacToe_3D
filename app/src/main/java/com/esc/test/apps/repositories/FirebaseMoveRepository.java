package com.esc.test.apps.repositories;

import static com.esc.test.apps.utils.DatabaseConstants.FRIENDS;
import static com.esc.test.apps.utils.DatabaseConstants.GAMES;
import static com.esc.test.apps.utils.DatabaseConstants.MOVE;
import static com.esc.test.apps.utils.DatabaseConstants.MOVES;
import static com.esc.test.apps.utils.DatabaseConstants.USERS;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.esc.test.apps.data.datastore.GameState;
import com.esc.test.apps.data.datastore.UserDetail;
import com.esc.test.apps.data.datastore.UserPreferences;
import com.esc.test.apps.network.FirebaseQueryLiveData;
import com.esc.test.apps.data.pojos.MoveInfo;
import com.esc.test.apps.utils.ExecutorFactory;
import com.esc.test.apps.utils.SingleLiveEvent;
import com.esc.test.apps.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

@Singleton
public class FirebaseMoveRepository {

    private final DatabaseReference ref;
    private final GameState gameState;
    private final UserDetail user;
    private final UserPreferences userPref;
    private final SingleLiveEvent<List<MoveInfo>> existingMoves = new SingleLiveEvent<>();
    private final List<MoveInfo> tempItems = new ArrayList<>();
    private final ExecutorService executor = ExecutorFactory.getSingleExecutor();
    private Disposable d;
    private String uid;
    private static final String TAG = "myT";

    @Inject
    public FirebaseMoveRepository (DatabaseReference ref, GameState gameState,
                                   UserDetail user, UserPreferences userPref
    ) {
        this.gameState = gameState;
        this.ref = ref;
        this.user = user;
        this.userPref = userPref;

        d = userPref.getUserPreference().subscribeOn(AndroidSchedulers.mainThread()).doOnNext(prefs -> {
            uid = prefs.getUid();
            Utils.dispose(d);
        }).subscribe();
    }

    public void addMove(MoveInfo move) {
        move.setUid(uid);
        ref.child(GAMES).child(gameState.getGameSetID()).child(gameState.getGameID()).child(MOVES)
                .child(String.valueOf(move.getMoveID())).setValue(move).addOnCompleteListener(task ->
                Log.d(TAG, "Move " + move.getPosition() + " uploaded"));
    }

    public LiveData<MoveInfo> getMoveInfo() {
        DatabaseReference moveRef = ref.child(USERS).child(uid).child(FRIENDS)
                .child(getFriendUid(gameState.getGameSetID())).child(MOVE);
        FirebaseQueryLiveData moveLiveData = new FirebaseQueryLiveData(moveRef);
        return Transformations.map(moveLiveData, snapshot -> {
           MoveInfo move = snapshot.getValue(MoveInfo.class);
           if (move != null && !move.getUid().equals(uid)) return move;
           else return null;
        });
    }

    private void getMoves(DataSnapshot dataSnapshot) {
        executor.execute(() -> {
            tempItems.clear();
            for(DataSnapshot snap : dataSnapshot.getChildren()){
                MoveInfo msg = snap.getValue(MoveInfo.class);
                if (msg != null) tempItems.add(msg);
            }
            existingMoves.postValue(tempItems);
        });
    }

    public void checkCurrentGameMoves(DatabaseReference movesRef) {
        movesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getMoves(snapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getFriendUid(String gameSetId) {
        String[] uids = gameSetId.split("_");
        return uids[0].equals(uid) ? uids[1] : uids[0];
    }

    public LiveData<List<MoveInfo>> getExistingMoves() { return existingMoves; }
}
