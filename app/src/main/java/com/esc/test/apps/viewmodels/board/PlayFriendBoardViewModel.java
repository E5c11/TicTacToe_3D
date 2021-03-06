package com.esc.test.apps.viewmodels.board;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.R;
import com.esc.test.apps.datastore.GameState;
import com.esc.test.apps.datastore.UserDetails;
import com.esc.test.apps.entities.Move;
import com.esc.test.apps.network.ConnectionLiveData;
import com.esc.test.apps.other.MovesFactory;
import com.esc.test.apps.pojos.CubeID;
import com.esc.test.apps.pojos.MoveInfo;
import com.esc.test.apps.pojos.Turn;
import com.esc.test.apps.repositories.FirebaseGameRepository;
import com.esc.test.apps.repositories.FirebaseMoveRepository;
import com.esc.test.apps.repositories.GameRepository;
import com.esc.test.apps.repositories.MoveRepository;
import com.esc.test.apps.utils.Utils;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@HiltViewModel
public class PlayFriendBoardViewModel extends ViewModel {

    private final LiveData<List<MoveInfo>> existingMoves;
    private final LiveData<String> turn;
    private String friendGamePiece;
    private Disposable d;
    private int moveCount = 0;
    private final UserDetails userDetails;
    private final GameState gameState;
    private final Application app;
    private final MoveRepository moveRepository;
    private final GameRepository gameRepository;
    private final MovesFactory moves;
    private final FirebaseGameRepository fbGameRepo;
    private final FirebaseMoveRepository fbMoveRepo;
    private final ConnectionLiveData network;
    public static final String TAG = "myT";

    @Inject
    public PlayFriendBoardViewModel(GameState gameState, MoveRepository moveRepository,
                                    GameRepository gameRepository, Application app,
                                    UserDetails userDetails, FirebaseGameRepository fbGameRepo,
                                    FirebaseMoveRepository fbMoveRepo, MovesFactory moves,
                                    ConnectionLiveData network
    ) {
        this.app = app;
        this.moveRepository = moveRepository;
        this.gameRepository = gameRepository;
        this.gameState = gameState;
        this.userDetails = userDetails;
        this.fbGameRepo = fbGameRepo;
        this.fbMoveRepo = fbMoveRepo;
        this.moves = moves;
        this.network = network;
        existingMoves = fbMoveRepo.getExistingMoves();
        turn = LiveDataReactiveStreams.fromPublisher(gameRepository.getTurn()
                .subscribeOn(Schedulers.io()));
    }

    public void getGameUids(String uids, boolean friendStarts) {
        friendGamePiece = friendGamePiece(friendStarts);
        fbGameRepo.getGameUID(uids);
    }

    public String friendGamePiece(boolean friendStarts) {
        return friendStarts ? app.getString(R.string.cross) : app.getString(R.string.circle);
    }

    public void newMove(CubeID cubeID) {
        d = gameRepository.getTurn().subscribeOn(Schedulers.io()).doOnNext(t -> {
            moves.createMoves(cubeID.getCoordinates(), t, String.valueOf(moveCount), true);
            Utils.dispose(d);
        }).subscribe();
    }

    public void addExistingMoves(List<MoveInfo> previousMoves) {
        MoveInfo[] moves = new MoveInfo[previousMoves.size()];
        moveRepository.insertMultipleMoves(previousMoves.toArray(moves));
    }

    public void uploadWinner() {
        if (friendGamePiece != null) {
            fbGameRepo.endGame(userDetails.getUid());
        }
    }

    public LiveData<Turn> getTurn() {
        return Transformations.map(turn, turnResult -> {
//            Log.d(TAG, "getTurnResult: " + turnResult);
            if (turnResult.equals(friendGamePiece) && gameState.isWinner() == null)
                return new Turn(turnResult, true);
            else if (gameState.isWinner() == null) return new Turn(turnResult, false);
            else return null;
        });
    }

    public LiveData<MoveInfo> getMoveInfo() {
        return Transformations.map(fbMoveRepo.getMoveInfo(), friendMove -> {
            if (friendMove != null)
                moveCount = Integer.parseInt(friendMove.getMoveID()) + 1;
            return friendMove;
        });}

    public LiveData<List<MoveInfo>> getExistingMoves() { return existingMoves; }

    public LiveData<Boolean> getNetwork() { return network; }
}


