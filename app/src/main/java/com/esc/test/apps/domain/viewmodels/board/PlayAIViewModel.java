package com.esc.test.apps.domain.viewmodels.board;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.R;
import com.esc.test.apps.common.helpers.move.CheckMoveFactory;
import com.esc.test.apps.common.helpers.move.BotMoveGenerator;
import com.esc.test.apps.data.persistence.GamePreferences;
import com.esc.test.apps.data.persistence.UserPreferences;
import com.esc.test.apps.data.models.entities.Move;
import com.esc.test.apps.data.models.pojos.CubeID;
import com.esc.test.apps.data.repositories.implementations.local.MoveRepository;
import com.esc.test.apps.common.utils.ExecutorFactory;
import com.esc.test.apps.common.utils.Utils;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class PlayAIViewModel extends ViewModel {

    private final CheckMoveFactory checkMoveFactory;
    private final Application app;
    private final MoveRepository moveRepo;
    private final BotMoveGenerator botMoveGenerator;
    private final ExecutorService executor = ExecutorFactory.getSingleExecutor();
    public final LiveData<String> error;
    private final UserPreferences userPref;
    private final GamePreferences gamePref;
    private Disposable d;
    private int moveCount;
    private int userMovePos;
    private String userPiece;
    private static final String TAG = "myT";
    public static final String AI_GAME = "play_ai";

    @Inject
    public PlayAIViewModel(CheckMoveFactory checkMoveFactory, Application app, MoveRepository moveRepo,
                           BotMoveGenerator botMoveGenerator, UserPreferences userPref, GamePreferences gamePref
    ) {
        this.checkMoveFactory = checkMoveFactory;
        this.app = app;
        this.moveRepo = moveRepo;
        this.botMoveGenerator = botMoveGenerator;
        this.userPref = userPref;
        this.gamePref = gamePref;
        firstMove();
        catchLastMove();
        error = botMoveGenerator.getError();
        botMoveGenerator.newGame();
    }

    public void firstMove() {
//        boolean firstMove = false;
//        moveCount = firstMove ? 0 : 1;
//        if (firstMove) {
//         userPiece = app.getString(R.string.circle);
//            int pos = getRandomPos();
//         executor.execute(() -> aiMoves.setFirstMove(pos, app.getString(R.string.cross), moveCount));
//            Log.d(TAG, "firstMove: ai " + pos);
//        } else {
         userPiece = app.getString(R.string.cross);
         botMoveGenerator.setPiece(app.getString(R.string.circle), 1);
         Log.d(TAG, "firstMove: user ");
//        }
    }

    public void newGame() { botMoveGenerator.newGame(); }

    public void newMove(CubeID cube) {
        moveCount++;
        checkMoveFactory.createMoves(
                cube.getCoordinates(), userPiece, String.valueOf(moveCount), false);
    }

    private void newAIMove(Move move) {
        moveCount++;
        d = gamePref.getGamePreference().subscribeOn(Schedulers.io()).doOnNext( pref -> {
            if (pref.getWinner().isEmpty()) executor.execute(() -> botMoveGenerator.eliminateLines(move));
            Utils.dispose(d);
        }).subscribe();
    }

    public void catchLastMove() {
        moveRepo.getLastMove().subscribeOn(Schedulers.io()).doOnNext(move -> {

        }).subscribe();
    }

    public LiveData<Move> getLastMove() {
        return LiveDataReactiveStreams.fromPublisher(moveRepo.getLastMove()
            .subscribeOn(Schedulers.io()).map( move -> {
                if (userPiece.equals(move.getPiece_played())) {
                    newAIMove(move);
                    return null;
                } else return move;
            }));
    }

    public void setLevel(CharSequence level) {
        userPref.updateLevelJava(level.toString());
    }

}
