package com.esc.test.apps.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.R;
import com.esc.test.apps.datastore.GameState;
import com.esc.test.apps.datastore.UserDetails;
import com.esc.test.apps.entities.Move;
import com.esc.test.apps.network.FirebaseQueryLiveData;
import com.esc.test.apps.pojos.Turn;
import com.esc.test.apps.repositories.GameRepository;
import com.esc.test.apps.repositories.MoveRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.schedulers.Schedulers;

@HiltViewModel
public class PlayFriendBoardViewModel extends ViewModel {

    private final MutableLiveData<List<Move>>existingMoves = new MutableLiveData<>();
    private final LiveData<String> turn;
    private String gameSetID;
    private String friendUID;
    private String friendGamePiece;
    private final List<Move> tempItems = new ArrayList<>();
    private ValueEventListener playerUIDsListener;
    private DatabaseReference movesRef;
    private final DatabaseReference userRef;
    private final DatabaseReference gameRef;
    private final UserDetails userDetails;
    private final GameState gameState;
    private final Application app;
    private final MoveRepository moveRepository;
    public static final String TAG = "myT";
    private static final String USERS = "users";
    private static final String FRIENDS = "friends";
    private static final String GAMES = "games";
    private static final String MOVES = "moves";
    private static final String ACTIVE_GAME = "active_game";
    private static final String STARTER = "starter";
    private static final String WINNER = "winner";
    private static final String GAME_ACTIVE = "game_active";

    @Inject
    public PlayFriendBoardViewModel(GameState gameState, MoveRepository moveRepository,
                                    GameRepository gameRepository, Application app,
                                    DatabaseReference db, UserDetails userDetails
    ) {
        this.app = app;
        this.moveRepository = moveRepository;
        this.gameState = gameState;
        this.userDetails = userDetails;
        turn = LiveDataReactiveStreams.fromPublisher(gameRepository.getTurn().subscribeOn(Schedulers.io()));
        userRef = db.child(USERS);
        gameRef = db.child(GAMES);
        setEventListener();
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
        Move msg;
        Log.d(TAG, "New move downloaded: " + dataSnapshot.getChildrenCount());
        for(DataSnapshot snap : dataSnapshot.getChildren()){
            msg = snap.getValue(Move.class);
            tempItems.add(msg);
        }
    }

    public MutableLiveData<List<Move>> getExistingMoves() {return existingMoves;}

    public void getGameUID(String uids, boolean friendStarts) {
        gameSetID = uids;
        gameState.setGameSetID(uids);
        String[] players = uids.split("_");
        if (players[0].equals(userDetails.getUid())) {
            userRef.child(players[1]).child(FRIENDS).child(players[0])
                    .child(ACTIVE_GAME).addListenerForSingleValueEvent(playerUIDsListener);
            friendUID = players[1];
        }
        else {
           userRef.child(players[0]).child(FRIENDS).child(players[1])
                    .child(ACTIVE_GAME).addListenerForSingleValueEvent(playerUIDsListener);
            friendUID = players[0];
        }
        friendGamePiece(friendStarts);
    }

    private void setEventListener() {
        playerUIDsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String ref = snapshot.getValue().toString();
                    movesRef = gameRef.child(gameSetID).child(ref).child(MOVES);
                    //movesRef = db.child(rp.getString(R.string.moves)).child(ref);
                    checkCurrentGameMoves(movesRef);
                    Log.d(TAG, "onDataChange: " + ref);
                    gameState.setGameID(ref);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    public void addExistingMoves(List<Move> previousMoves) {
        Move[] moves = new Move[previousMoves.size()];
        moveRepository.insertMultipleMoves(previousMoves.toArray(moves));
    }

    private void checkCurrentGameMoves(DatabaseReference ref) {
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

    public void uploadWinner() {
        if (friendGamePiece != null) {
            String gameID = gameState.getGameID();
            Log.d(TAG, "uploadWinner: gamesetid: " + gameSetID + " gameid: " + gameID);
            endGame(userDetails.getUid());
        }
    }

    public void endGame(String winner) {
        if (winner == null) winner = friendUID;
        gameRef.child(gameState.getGameSetID()).child(gameState.getGameID()).child(GAME_ACTIVE).setValue(false);
        userRef.child(userDetails.getUid()).child(FRIENDS).child(friendUID).child(ACTIVE_GAME).removeValue();
        userRef.child(friendUID).child(FRIENDS).child(userDetails.getUid()).child(ACTIVE_GAME).removeValue();
        userRef.child(userDetails.getUid()).child(FRIENDS).child(friendUID).child(STARTER).removeValue();
        userRef.child(friendUID).child(FRIENDS).child(userDetails.getUid()).child(STARTER).removeValue();
        gameRef.child(gameSetID).child(gameState.getGameID()).child(WINNER).setValue(winner);
    }

    public void friendGamePiece(boolean friendStarts) {
        if (friendStarts) friendGamePiece = app.getString(R.string.cross);
        else friendGamePiece = app.getString(R.string.circle);
    }

    public LiveData<Turn> getTurn() {
        return Transformations.map(turn, turnResult -> {
            Log.d(TAG, "getTurnResult: " + turnResult);
            if (turnResult.equals(friendGamePiece) && gameState.isWinner() == null)
                return new Turn(turnResult, true);
            else if (gameState.isWinner() == null) return new Turn(turnResult, false);
            else return null;
        });
    }
}


