package com.esc.test.apps.domain.viewmodels.board;

import static com.esc.test.apps.common.utils.Utils.dispose;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.R;
import com.esc.test.apps.common.helpers.move.CheckMoveFactory;
import com.esc.test.apps.common.network.ConnectionLiveData;
import com.esc.test.apps.common.utils.SingleLiveEvent;
import com.esc.test.apps.data.models.pojos.CubeID;
import com.esc.test.apps.data.models.pojos.MoveInfo;
import com.esc.test.apps.data.models.pojos.Turn;
import com.esc.test.apps.data.persistence.GamePreferences;
import com.esc.test.apps.data.persistence.UserPreferences;
import com.esc.test.apps.data.repositories.FbGameRepo;
import com.esc.test.apps.data.repositories.FbMoveRepo;
import com.esc.test.apps.data.repositories.implementations.local.GameRepository;
import com.esc.test.apps.board.moves.MoveRepository;

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
    private final CheckMoveFactory moves;
    private final FbGameRepo fbGameRepo;
    private final FbMoveRepo fbMoveRepo;
    public final ConnectionLiveData network;
    public static final String TAG = "myT";

    @Inject
    public PlayFriendBoardViewModel(MoveRepository moveRepository, GameRepository gameRepository,
                                    Application app, FbGameRepo fbGameRepo,
                                    GamePreferences gamePref, FbMoveRepo fbMoveRepo,
                                    CheckMoveFactory moves, ConnectionLiveData network, UserPreferences userPref
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
            Map.of("uid", user.getUid(), "gameSetId", game.getSetId())
        ).subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread()).doOnNext(pref -> {
            moveInfo = getMoveInfo(pref.get("uid"), pref.get("gameSetId"));
            _movesReady.postValue(true);
            dispose(d);
        }).subscribe();

        f = fbGameRepo.getGameId().subscribeOn(Schedulers.io()).doOnNext( quit -> {
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


