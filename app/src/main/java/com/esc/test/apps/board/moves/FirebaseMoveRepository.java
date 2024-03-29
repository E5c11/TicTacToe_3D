package com.esc.test.apps.board.moves;

import static com.esc.test.apps.common.utils.DatabaseConstants.FRIENDS;
import static com.esc.test.apps.common.utils.DatabaseConstants.GAMES;
import static com.esc.test.apps.common.utils.DatabaseConstants.MOVE;
import static com.esc.test.apps.common.utils.DatabaseConstants.MOVES;
import static com.esc.test.apps.common.utils.DatabaseConstants.USERS;
import static com.esc.test.apps.common.utils.Utils.dispose;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.esc.test.apps.board.games.io.GamePreferences;
import com.esc.test.apps.data.persistence.UserPreferences;
import com.esc.test.apps.board.moves.data.MoveResponse;
import com.esc.test.apps.data.repositories.FbMoveRepo;
import com.esc.test.apps.data.source.remote.FirebaseQueryLiveData;
import com.esc.test.apps.common.utils.ExecutorFactory;
import com.esc.test.apps.common.utils.SingleLiveEvent;
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
import io.reactivex.rxjava3.schedulers.Schedulers;

@Singleton
public class FirebaseMoveRepository implements FbMoveRepo {

    private final DatabaseReference ref;
    private final GamePreferences gamePref;
    private final SingleLiveEvent<List<MoveResponse>> existingMoves = new SingleLiveEvent<>();
    private final List<MoveResponse> tempItems = new ArrayList<>();
    private final ExecutorService executor = ExecutorFactory.getSingleExecutor();
    private Disposable d;
    private String uid;
    private static final String TAG = "myT";

    @Inject
    public FirebaseMoveRepository (DatabaseReference ref, UserPreferences userPref, GamePreferences gamePref
    ) {
        this.ref = ref;
        this.gamePref = gamePref;
        d = userPref.getUserPreference().subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
            .doOnNext(prefs -> {
                uid = prefs.getUid();
                dispose(d);
        }).subscribe();
    }

    @Override
    public void addMove(MoveResponse move) {
        move.setUserId(uid);
        d = gamePref.getGamePreference().subscribeOn(Schedulers.io()).doOnNext( pref -> {
            ref.child(GAMES).child(pref.getSetId()).child(pref.getId()).child(MOVES)
                    .child(move.getId())
                    .setValue(move)
                    .addOnCompleteListener(task -> Log.d(TAG, "Move " + move.getPosition() + " uploaded"));
            dispose(d);
        }).subscribe();
    }

    @Override
    public LiveData<MoveResponse> getMoveInfo(@NonNull String uid, String gameSetId) {
        DatabaseReference moveRef = ref.child(USERS).child(uid).child(FRIENDS)
                .child(getFriendUid(gameSetId)).child(MOVE);
        FirebaseQueryLiveData moveLiveData = new FirebaseQueryLiveData(moveRef);
        return Transformations.map(moveLiveData, snapshot -> {
           MoveResponse move = snapshot.getValue(MoveResponse.class);
           if (move != null && !move.getUserId().equals(uid)) return move;
           else return null;
        });
    }

    private void getMoves(DataSnapshot dataSnapshot) {
        executor.execute(() -> {
            tempItems.clear();
            for(DataSnapshot snap : dataSnapshot.getChildren()){
                MoveResponse msg = snap.getValue(MoveResponse.class);
                if (msg != null) tempItems.add(msg);
            }
            existingMoves.postValue(tempItems);
        });
    }

    @Override
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

    @Override
    public LiveData<List<MoveResponse>> getExistingMoves() { return existingMoves; }
}
