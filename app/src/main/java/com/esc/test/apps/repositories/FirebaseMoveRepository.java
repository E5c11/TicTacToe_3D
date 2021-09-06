package com.esc.test.apps.repositories;

import android.util.Log;

import androidx.annotation.NonNull;

import com.esc.test.apps.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.esc.test.apps.datastore.GameState;
import com.esc.test.apps.other.ResourceProvider;
import com.esc.test.apps.entities.Move;

@Singleton
public class FirebaseMoveRepository {

    private final DatabaseReference ref;
    private final GameState gameState;
    private final ResourceProvider rp;
    private static final String TAG = "myT";

    @Inject
    public FirebaseMoveRepository (DatabaseReference ref, GameState gameState, ResourceProvider rp) {
        this.gameState = gameState;
        this.ref = ref.child(rp.getString(R.string.games));
        this.rp = rp;
    }

    public void addMove(Move move) {
        String cood, pos, piecePlayed;
        cood = move.getCoordinates();
        pos = move.getPosition();
        piecePlayed = move.getPiece_played();
        Log.d(TAG, "addMove: " + gameState.getGameID());
        ref.child(gameState.getGameSetID()).child(gameState.getGameID()).child(rp.getString(R.string.moves)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int id;
                if (snapshot.exists()) {
                    id = (int) snapshot.getChildrenCount();
                } else id = 0;
                Log.d(TAG, "onDataChange: move id " + id);
                Move moveInfo = new Move(cood, pos, piecePlayed);
                ref.child(gameState.getGameSetID()).child(gameState.getGameID()).child(rp.getString(R.string.moves)).child(String.valueOf(id)).setValue(moveInfo).addOnCompleteListener(task ->
                    Log.d(TAG, "Move " + pos + " uploaded"));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
