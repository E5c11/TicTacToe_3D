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
import com.esc.test.apps.pojos.MoveInfo;
import com.esc.test.apps.pojos.Turn;
import com.esc.test.apps.repositories.FirebaseGameRepository;
import com.esc.test.apps.repositories.FirebaseMoveRepository;
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

    private final LiveData<List<Move>> existingMoves;
    private final LiveData<String> turn;
    private String friendGamePiece;
    private final UserDetails userDetails;
    private final GameState gameState;
    private final Application app;
    private final MoveRepository moveRepository;
    private final FirebaseGameRepository fbGameRepo;
    private final FirebaseMoveRepository fbMoveRepo;
    public static final String TAG = "myT";

    @Inject
    public PlayFriendBoardViewModel(GameState gameState, MoveRepository moveRepository,
                                    GameRepository gameRepository, Application app,
                                    UserDetails userDetails, FirebaseGameRepository fbGameRepo,
                                    FirebaseMoveRepository fbMoveRepo
    ) {
        this.app = app;
        this.moveRepository = moveRepository;
        this.gameState = gameState;
        this.userDetails = userDetails;
        this.fbGameRepo = fbGameRepo;
        this.fbMoveRepo = fbMoveRepo;
        existingMoves = fbMoveRepo.getExistingMoves();
        turn = LiveDataReactiveStreams.fromPublisher(gameRepository.getTurn().subscribeOn(Schedulers.io()));
    }

    public void getGameUids(String uids, boolean friendStarts) {
        friendGamePiece = friendGamePiece(friendStarts);
        fbGameRepo.getGameUID(uids, friendGamePiece);
    }

    public String friendGamePiece(boolean friendStarts) {
        return friendStarts ? app.getString(R.string.cross) : app.getString(R.string.circle);
    }

    public void addExistingMoves(List<Move> previousMoves) {
        Move[] moves = new Move[previousMoves.size()];
        moveRepository.insertMultipleMoves(previousMoves.toArray(moves));
    }

    public void quitGame() { fbGameRepo.endGame(null);}

    public void uploadWinner() {
        if (friendGamePiece != null) {
            fbGameRepo.endGame(userDetails.getUid());
        }
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

    public LiveData<Move> getMoveInfo() { return fbMoveRepo.getMoveInfo();}

    public LiveData<List<Move>> getExistingMoves() { return existingMoves; }
}


