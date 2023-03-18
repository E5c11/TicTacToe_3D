package com.esc.test.apps.domain.viewmodels.board;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.R;
import com.esc.test.apps.common.helpers.move.BotMoveGenerator;
import com.esc.test.apps.common.helpers.move.CheckMoveFactory;
import com.esc.test.apps.data.models.entities.Move;
import com.esc.test.apps.data.models.pojos.CubeID;
import com.esc.test.apps.data.persistence.GamePreferences;
import com.esc.test.apps.data.persistence.UserPreferences;
import com.esc.test.apps.data.repositories.implementations.local.MoveRepository;
import com.esc.test.apps.domain.usecases.moves.MovesUsecase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class PlayAIViewModel extends ViewModel {

    private final Application app;
    private final MoveRepository moveRepo;
    public final LiveData<String> error;
    private final UserPreferences userPref;
    private final MovesUsecase movesUsecase;
    private int moveCount;
    private String userPiece;
    private static final String TAG = "myT";
    public static final String AI_GAME = "play_ai";

    @Inject
    public PlayAIViewModel(Application app, MoveRepository moveRepo, BotMoveGenerator botMoveGenerator,
                           UserPreferences userPref, MovesUsecase movesUsecase
    ) {
        this.app = app;
        this.moveRepo = moveRepo;
        this.userPref = userPref;
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
