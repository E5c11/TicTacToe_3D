package com.esc.test.apps.viewmodels.board;

import static com.esc.test.apps.other.MoveUtils.getRandomPos;
import static com.esc.test.apps.other.MoveUtils.getStringCoord;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.R;
import com.esc.test.apps.entities.Move;
import com.esc.test.apps.other.AIMoves;
import com.esc.test.apps.other.MovesFactory;
import com.esc.test.apps.pojos.CubeID;
import com.esc.test.apps.repositories.MoveRepository;

import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

@HiltViewModel
public class PlayAIViewModel extends ViewModel {

    private final MovesFactory movesFactory;
    private final Application app;
    private final MoveRepository moveRepo;
    private AIMoves aiMoves;
    private int moveCount;
    private int userMovePos;
    private String userPiece;

    @Inject
    public PlayAIViewModel(MovesFactory movesFactory, Application app, MoveRepository moveRepo,
                           AIMoves aiMoves

    ) {
        this.movesFactory = movesFactory;
        this.app = app;
        this.moveRepo = moveRepo;
        this.aiMoves = aiMoves;
        catchLastMove();
    }

    public void firstMove(boolean firstMove, String piecePlayed) {
         moveCount = firstMove ? 0 : 1;
         int pos = getRandomPos();
         if (firstMove) {
             userPiece = app.getString(R.string.circle);
             aiMoves.setFirstMove(pos, app.getString(R.string.cross), moveCount);
         } else {
             if (pos == userMovePos) pos = getRandomPos(userMovePos);
             userPiece = app.getString(R.string.cross);
             aiMoves.setFirstMove(pos, app.getString(R.string.circle), moveCount);
         }
    }

    public void newMove(CubeID cube) {

    }

    private void newAIMove(Move move) {
        aiMoves.eliminateLines(move);
    }

    public void catchLastMove() {
        moveRepo.getLastMove().subscribeOn(Schedulers.io()).doOnNext(move -> {
            if (userPiece.equals(move.getPiece_played())) newAIMove(move);
        }).subscribe();
    }
}
