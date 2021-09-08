package com.esc.test.apps.repositories;

import android.util.Log;

import androidx.annotation.NonNull;

import com.esc.test.apps.datastore.GameState;
import com.esc.test.apps.entities.Move;
import com.esc.test.apps.other.MovesFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseMoveRepository {

    private final DatabaseReference ref;
    private final GameState gameState;
    private static final String TAG = "myT";
    private static final String MOVES = "moves";
    private static final String GAMES = "games";

    @Inject
    public FirebaseMoveRepository (DatabaseReference ref, GameState gameState) {
        this.gameState = gameState;
        this.ref = ref.child(GAMES);
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
                Move moveInfo = new Move(cood, pos, piecePlayed);
                ref.child(gameState.getGameSetID()).child(gameState.getGameID()).child(MOVES)
                        .child(String.valueOf(id)).setValue(moveInfo).addOnCompleteListener(task ->
                            Log.d(TAG, "Move " + pos + " uploaded"));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
