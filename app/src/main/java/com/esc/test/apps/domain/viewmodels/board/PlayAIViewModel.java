package com.esc.test.apps.domain.viewmodels.board;

import static com.esc.test.apps.common.utils.Utils.dispose;

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
import com.esc.test.apps.domain.usecases.moves.MovesUsecase;

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
    public final LiveData<String> error;
    private final UserPreferences userPref;
    private final GamePreferences gamePref;
    private final MovesUsecase movesUsecase;
    private Disposable d;
    private int moveCount;
    private String userPiece;
    private static final String TAG = "myT";
    public static final String AI_GAME = "play_ai";

    @Inject
    public PlayAIViewModel(CheckMoveFactory checkMoveFactory, Application app, MoveRepository moveRepo,
                           BotMoveGenerator botMoveGenerator, UserPreferences userPref,
                           GamePreferences gamePref, MovesUsecase movesUsecase
    ) {
        this.checkMoveFactory = checkMoveFactory;
        this.app = app;
        this.moveRepo = moveRepo;
        this.botMoveGenerator = botMoveGenerator;
        this.userPref = userPref;
        this.gamePref = gamePref;
        this.movesUsecase = movesUsecase;
        firstMove();
        error = botMoveGenerator.getError();
    }

    public void firstMove() {
        userPiece = app.getString(R.string.cross);
        movesUsecase.invoke(app.getString(R.string.circle), 1);
    }

    public void newGame() { movesUsecase.invoke(); }

    public void newMove(CubeID cube) {
        movesUsecase.invoke(cube.getCoordinates(), userPiece, ++moveCount, false);
    }

    private void newAIMove(Move move) {
        moveCount++;
        movesUsecase.invoke(move);
    }

    public LiveData<Move> getLastMove() {
        return LiveDataReactiveStreams.fromPublisher(moveRepo.getLastMove()
            .subscribeOn(Schedulers.io()).map( move -> {
                if (userPiece.equals(move.getPiece_played())) {
                    newAIMove(move);
                    return move.emptyMove();
                } else return move;
            }));
    }

    public void setLevel(CharSequence level) {
        userPref.updateLevelJava(level.toString());
    }

}
