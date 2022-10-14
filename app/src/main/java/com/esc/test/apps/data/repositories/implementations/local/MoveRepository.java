package com.esc.test.apps.data.repositories.implementations.local;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.esc.test.apps.data.objects.entities.Move;
import com.esc.test.apps.data.source.local.GameMovesDao;
import com.esc.test.apps.data.objects.pojos.MoveInfo;
import com.esc.test.apps.common.utils.ExecutorFactory;

import io.reactivex.rxjava3.core.Flowable;

@Singleton
public class MoveRepository {
    private final GameMovesDao gameMovesDao;
    private final ExecutorService service  = ExecutorFactory.getFixedSizeExecutor();

    @Inject
    public MoveRepository(GameMovesDao gameMovesDao) {
        this.gameMovesDao = gameMovesDao;
    }

    public void insertMove(Move move) {
        service.submit(() -> gameMovesDao.insert(move));
    }

    public void insertMultipleMoves(MoveInfo...moves) {
        service.submit(() -> {
            Move[] changed = new Move[moves.length];
            int i = 0;
            for (MoveInfo move : moves) {

                changed[i] =
                        new Move(move.getCoordinates(), move.getPosition(), move.getPiece_played());
                i++;
            }
            gameMovesDao.insertMoves(changed);
        });
    }

    public void deleteGameMoves() {
        service.submit(gameMovesDao::deleteGame);
    }

    public LiveData<String> getMovePosition(String position) {
        return gameMovesDao.getPosition(position);
    }

    public LiveData<String> getFirstMove() {
        return gameMovesDao.getFirstMove();
    }

    public Flowable<Move> getLastMove() {
        return gameMovesDao.getMove();
    }

    public String getOccupiedWith(String position) {
        long start = System.nanoTime();
        Future<String> checkPieceAtPos = service.submit(new Task(gameMovesDao, position));

        try {
            return checkPieceAtPos.get(100, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            return null;
        }
    }

    public List<Move> getAllMoves() {
//        long start = System.nanoTime();
        Callable<List<Move>> call = gameMovesDao::getAllMoves;
        Future<List<Move>> checkAllMoves = service.submit(call);

        try {
            return checkAllMoves.get(100, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            return null;
        }
    }

    static class Task implements Callable<String> {
        private final GameMovesDao gameMovesDao;
        private final String position;

        public Task(GameMovesDao gameMovesDao, String position) {
            this.gameMovesDao = gameMovesDao;
            this.position = position;
        }

        @Override
        public String call() {
            return gameMovesDao.getPiece_played(position);
        }
    }

}
