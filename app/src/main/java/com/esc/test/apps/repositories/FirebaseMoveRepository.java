package com.esc.test.apps.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.esc.test.apps.datastore.GameState;
import com.esc.test.apps.datastore.UserDetails;
import com.esc.test.apps.entities.Move;
import com.esc.test.apps.network.FirebaseQueryLiveData;
import com.esc.test.apps.other.MovesFactory;
import com.esc.test.apps.pojos.MoveInfo;
import com.esc.test.apps.utils.SingleLiveEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseMoveRepository {

    private final DatabaseReference ref;
    private final GameState gameState;
    private final UserDetails user;
    private final SingleLiveEvent<List<MoveInfo>> existingMoves = new SingleLiveEvent<>();
    private final List<MoveInfo> tempItems = new ArrayList<>();
    private static final String TAG = "myT";
    private static final String MOVES = "moves";
    private static final String GAMES = "games";
    private static final String FRIENDS = "friends";
    private static final String USERS = "users";
    private static final String MOVE = "move";
    private String friendGamePiece;

    @Inject
    public FirebaseMoveRepository (DatabaseReference ref, GameState gameState, UserDetails user) {
        this.gameState = gameState;
        this.ref = ref;
        this.user = user;
    }

    public void addMove(MoveInfo move) {
        move.setUid(user.getUid());
        Log.d(TAG, "addMove: " + gameState.getGameID());
        ref.child(GAMES).child(gameState.getGameSetID()).child(gameState.getGameID()).child(MOVES)
                .child(String.valueOf(move.getMoveID())).setValue(move).addOnCompleteListener(task ->
                Log.d(TAG, "Move " + move.getPosition() + " uploaded"));
    }

    public LiveData<MoveInfo> getMoveInfo() {
        DatabaseReference moveRef = ref.child(USERS).child(user.getUid()).child(FRIENDS)
                .child(getFriendUid(gameState.getGameSetID())).child(MOVE);
        FirebaseQueryLiveData moveLiveData = new FirebaseQueryLiveData(moveRef);
        return Transformations.map(moveLiveData, snapshot -> {
            MoveInfo move = snapshot.getValue(MoveInfo.class);
            if (move != null)
                Log.d(TAG, "New move downloaded: " + move.getMoveID());
            return move;
        });
    }

    private void getMoves(DataSnapshot dataSnapshot) {
        tempItems.clear();

        for(DataSnapshot snap : dataSnapshot.getChildren()){
            MoveInfo msg = snap.getValue(MoveInfo.class);
            if (msg != null) tempItems.add(msg);
        }
    }

    public void checkCurrentGameMoves(DatabaseReference movesRef, String friendPiece) {
        friendGamePiece = friendPiece;
        movesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getMoves(snapshot);
                existingMoves.setValue(tempItems);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getFriendUid(String gameSetId) {
        String[] uids = gameSetId.split("_");
        return uids[0].equals(user.getUid()) ? uids[1] : uids[0];
    }

    public LiveData<List<MoveInfo>> getExistingMoves() { return existingMoves; }
}
