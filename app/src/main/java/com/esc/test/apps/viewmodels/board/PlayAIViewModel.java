package com.esc.test.apps.viewmodels.board;

import static com.esc.test.apps.other.MoveUtils.getRandomPos;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.R;
import com.esc.test.apps.entities.Move;
import com.esc.test.apps.other.AIMoves;
import com.esc.test.apps.other.MovesFactory;
import com.esc.test.apps.pojos.CubeID;
import com.esc.test.apps.repositories.MoveRepository;
import com.esc.test.apps.utils.ExecutorFactory;

import java.util.Random;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.schedulers.Schedulers;

@HiltViewModel
public class PlayAIViewModel extends ViewModel {

    private final MovesFactory movesFactory;
    private final Application app;
    private final MoveRepository moveRepo;
    private final AIMoves aiMoves;
    private final Random rand;
    private final ExecutorService executor = ExecutorFactory.getSingleExecutor();
    private final LiveData<Move> lastMove;
    private int moveCount;
    private int userMovePos;
    private String userPiece;
    private static final String TAG = "myT";

    @Inject
    public PlayAIViewModel(MovesFactory movesFactory, Application app, MoveRepository moveRepo,
                           AIMoves aiMoves, Random rand

    ) {
        this.movesFactory = movesFactory;
        this.app = app;
        this.moveRepo = moveRepo;
        this.aiMoves = aiMoves;
        this.rand = rand;
        firstMove();
        catchLastMove();
        lastMove = LiveDataReactiveStreams.fromPublisher(moveRepo.getLastMove()
                .subscribeOn(Schedulers.io()));
    }

    public void firstMove() {
        boolean firstMove = rand.nextBoolean();
        moveCount = firstMove ? 0 : 1;
        int pos = getRandomPos();
        if (firstMove) {
         userPiece = app.getString(R.string.circle);
         aiMoves.setFirstMove(pos, app.getString(R.string.cross), moveCount);
            Log.d(TAG, "firstMove: ai" + pos);
        } else {
         userPiece = app.getString(R.string.cross);
         aiMoves.setPiece(app.getString(R.string.circle));
         Log.d(TAG, "firstMove: user" + pos);
        }
    }

    public void newMove(CubeID cube) {
        moveCount++;
        movesFactory.createMoves(
                cube.getCoordinates(), userPiece, String.valueOf(moveCount), false);
    }

    private void newAIMove(Move move) {
        moveCount++;
        executor.execute(() -> aiMoves.eliminateLines(move));
    }

    public void catchLastMove() {
        moveRepo.getLastMove().subscribeOn(Schedulers.io()).doOnNext(move -> {

        }).subscribe();
    }

    public LiveData<Move> getLastMove() {
        return Transformations.map(lastMove, move -> {
            if (userPiece.equals(move.getPiece_played())) {
                newAIMove(move);
                return null;
            } else return move;
        });
    }
}
