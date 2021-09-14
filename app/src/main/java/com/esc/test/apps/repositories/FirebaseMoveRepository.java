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
    private DatabaseReference movesRef;
    private final GameState gameState;
    private final UserDetails user;
    private final MutableLiveData<List<Move>> existingMoves = new MutableLiveData<>();
    private final List<Move> tempItems = new ArrayList<>();
    private static final String TAG = "myT";
    private static final String MOVES = "moves";
    private static final String GAMES = "games";
    private String friendGamePiece;

    @Inject
    public FirebaseMoveRepository (DatabaseReference ref, GameState gameState, UserDetails user) {
        this.gameState = gameState;
        this.ref = ref.child(GAMES);
        this.user = user;
    }

    public void addMove(Move move) {
        String cood, pos, piecePlayed;
        cood = move.getCoordinates();
        pos = move.getPosition();
        piecePlayed = move.getPiece_played();
        Log.d(TAG, "addMove: " + gameState.getGameID());
        ref.child(gameState.getGameSetID()).child(gameState.getGameID()).child(MOVES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int id;
                if (snapshot.exists()) {
                    id = (int) snapshot.getChildrenCount();
                } else id = 0;
                Log.d(TAG, "onDataChange: move id " + id);
                MoveInfo moveInfo =
                        new MoveInfo(cood, pos, piecePlayed, String.valueOf(id), getFriendUid(gameState.getGameSetID()));
                ref.child(gameState.getGameSetID()).child(gameState.getGameID()).child(MOVES)
                        .child(String.valueOf(id)).setValue(moveInfo).addOnCompleteListener(task ->
                            Log.d(TAG, "Move " + pos + " uploaded"));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public LiveData<Move> getMoveInfo() {
        FirebaseQueryLiveData moveLiveData = new FirebaseQueryLiveData(movesRef);
        return Transformations.map(moveLiveData, dataSnapshot -> {
            getMoves(dataSnapshot);
            if (tempItems.isEmpty()) return null;
            else if (tempItems.get(tempItems.size()-1).getPiece_played().equals(friendGamePiece))
                return tempItems.get(tempItems.size()-1);
            else return null;
        });
    }

    private void getMoves(DataSnapshot dataSnapshot) {
        tempItems.clear();
        MoveInfo msg;
        Log.d(TAG, "New move downloaded: " + dataSnapshot.getChildrenCount());
        for(DataSnapshot snap : dataSnapshot.getChildren()){
            msg = snap.getValue(MoveInfo.class);
            if (msg != null) tempItems
                    .add(new Move(msg.getCoordinates(), msg.getPosition(), msg.getPiece_played()));
        }
    }

    public void checkCurrentGameMoves(DatabaseReference ref, String friendPiece) {
        friendGamePiece = friendPiece;
        movesRef = ref;
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
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

    public LiveData<List<Move>> getExistingMoves() { return existingMoves; }
}
