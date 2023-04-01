package com.esc.test.apps.board.moves;

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

import com.esc.test.apps.board.moves.data.Move;
import com.esc.test.apps.board.moves.data.MoveEntity;
import com.esc.test.apps.board.moves.io.MovesDao;
import com.esc.test.apps.board.moves.data.MoveResponse;
import com.esc.test.apps.common.utils.ExecutorFactory;

import io.reactivex.rxjava3.core.Flowable;

@Singleton
public class MoveRepositoryLegacy {
    private final MovesDao movesDao;
    private final ExecutorService service  = ExecutorFactory.getFixedSizeExecutor();

    @Inject
    public MoveRepositoryLegacy(MovesDao movesDao) {
        this.movesDao = movesDao;
    }

    public void insertMove(Move move) {
        service.submit(() -> movesDao.insert(move));
    }

    public void insertMultipleMoves(MoveResponse...moves) {
        service.submit(() -> {
            MoveEntity[] changed = new MoveEntity[moves.length];
            int i = 0;
            for (MoveResponse move : moves) {

                changed[i] =
                        new MoveEntity(move.getCoordinates(), move.getPosition(), move.getPiecePlayed());
                i++;
            }
            movesDao.insertMoves(changed);
        });
    }

    public void deleteGameMoves() {
        service.submit(movesDao::deleteGame);
    }

    public LiveData<String> getMovePosition(String position) {
        return movesDao.getPositionLd(position);
    }

    public LiveData<String> getFirstMove() {
        return movesDao.getFirstMoveLd();
    }

    public Flowable<MoveEntity> getLastMove() {
        return movesDao.getMoveRx();
    }

    public String getOccupiedWith(String position) {
        long start = System.nanoTime();
        Future<String> checkPieceAtPos = service.submit(new Task(movesDao, position));

        try {
            return checkPieceAtPos.get(100, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            return null;
        }
    }

    public List<MoveEntity> getAllMoves() {
//        long start = System.nanoTime();
        Callable<List<MoveEntity>> call = movesDao::getAllMoves;
        Future<List<MoveEntity>> checkAllMoves = service.submit(call);

        try {
            return checkAllMoves.get(100, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            return null;
        }
    }

    static class Task implements Callable<String> {
        private final MovesDao movesDao;
        private final String position;

        public Task(MovesDao movesDao, String position) {
            this.movesDao = movesDao;
            this.position = position;
        }

        @Override
        public String call() {
            return movesDao.getPiecePlayed(position);
        }
    }

}
