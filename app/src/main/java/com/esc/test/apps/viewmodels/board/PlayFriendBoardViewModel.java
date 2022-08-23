package com.esc.test.apps.viewmodels.board;

import static com.esc.test.apps.utils.Utils.dispose;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.R;
import com.esc.test.apps.adapters.move.MovesFactory;
import com.esc.test.apps.data.datastore.GamePreferences;
import com.esc.test.apps.data.datastore.UserPreferences;
import com.esc.test.apps.data.pojos.CubeID;
import com.esc.test.apps.data.pojos.MoveInfo;
import com.esc.test.apps.data.pojos.Turn;
import com.esc.test.apps.network.ConnectionLiveData;
import com.esc.test.apps.repositories.FirebaseGameRepository;
import com.esc.test.apps.repositories.FirebaseMoveRepository;
import com.esc.test.apps.repositories.GameRepository;
import com.esc.test.apps.repositories.MoveRepository;
import com.esc.test.apps.utils.SingleLiveEvent;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class PlayFriendBoardViewModel extends ViewModel {

    public LiveData<MoveInfo> moveInfo;
    private final SingleLiveEvent<Boolean> _movesReady = new SingleLiveEvent<>();
    public final SingleLiveEvent<Boolean> movesReady = _movesReady;
    private final SingleLiveEvent<Boolean> _winReady = new SingleLiveEvent<>();
    public final SingleLiveEvent<Boolean> winReady = _winReady;
    public LiveData<Map<String, String>> winState;
    public final LiveData<List<MoveInfo>> existingMoves;
    private String friendGamePiece;
    private Disposable d, f;
    private int moveCount = 0;
    private final UserPreferences userPref;
    private final GamePreferences gamePref;
    private final Application app;
    private final MoveRepository moveRepository;
    private final GameRepository gameRepository;
    private final MovesFactory moves;
    private final FirebaseGameRepository fbGameRepo;
    private final FirebaseMoveRepository fbMoveRepo;
    public final ConnectionLiveData network;
    public static final String TAG = "myT";

    @Inject
    public PlayFriendBoardViewModel(MoveRepository moveRepository, GameRepository gameRepository,
                                    Application app, FirebaseGameRepository fbGameRepo,
                                    GamePreferences gamePref, FirebaseMoveRepository fbMoveRepo,
                                    MovesFactory moves, ConnectionLiveData network, UserPreferences userPref
    ) {
        this.app = app;
        this.moveRepository = moveRepository;
        this.gameRepository = gameRepository;
        this.fbGameRepo = fbGameRepo;
        this.gamePref = gamePref;
        this.fbMoveRepo = fbMoveRepo;
        this.moves = moves;
        this.network = network;
        this.userPref = userPref;
        existingMoves = fbMoveRepo.getExistingMoves();

        d = Flowable.combineLatest(userPref.getUserPreference(), gamePref.getGamePreference(), (user, game) ->
            Map.of("uid", user.getUid(), "gameSetId", game.getGameSetId())
        ).subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread()).doOnNext(pref -> {
            moveInfo = getMoveInfo(pref.get("uid"), pref.get("gameSetId"));
            _movesReady.postValue(true);
            dispose(d);
        }).subscribe();

        f = fbGameRepo.gameId.subscribeOn(Schedulers.io()).doOnNext( quit -> {
            _winReady.postValue(true);
            winState = fbGameRepo.getGameActiveState();
            dispose(f);
        }).subscribe();
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
            dispose(d);
        }).subscribe();
    }

    public void addExistingMoves(List<MoveInfo> previousMoves) {
        MoveInfo[] moves = new MoveInfo[previousMoves.size()];
        moveRepository.insertMultipleMoves(previousMoves.toArray(moves));
    }

    public void uploadWinner() {
        if (friendGamePiece != null) {
            d = userPref.getUserPreference().subscribeOn(Schedulers.io()).doOnNext( pref -> {
                fbGameRepo.endGame(pref.getUid());
                dispose(d);
            }).subscribe();
        }
    }

    public LiveData<Turn> getTurn() {
        return LiveDataReactiveStreams.fromPublisher(
            Flowable.combineLatest(gameRepository.getTurn(), gamePref.getGamePreference(), (turn, pref) -> {
                if (turn.equals(friendGamePiece) && pref.getWinner().isEmpty())
                    return new Turn(turn, true);
                else if (pref.getWinner().isEmpty()) return new Turn(turn, false);
                else return new Turn("", false);
            }).subscribeOn(Schedulers.io())
        );
    }

    public LiveData<MoveInfo> getMoveInfo(String uid, String gameSetId) {
        return Transformations.map(fbMoveRepo.getMoveInfo(uid, gameSetId), friendMove -> {
            if (friendMove != null)
                moveCount = Integer.parseInt(friendMove.getMoveID()) + 1;
            return friendMove;
        });
    }

}


