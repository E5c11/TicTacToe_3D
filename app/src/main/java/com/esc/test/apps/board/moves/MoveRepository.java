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
import com.esc.test.apps.board.moves.io.GameMovesDao;
import com.esc.test.apps.board.moves.data.MoveResponse;
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

    public void insertMultipleMoves(MoveResponse...moves) {
        service.submit(() -> {
            MoveEntity[] changed = new MoveEntity[moves.length];
            int i = 0;
            for (MoveResponse move : moves) {

                changed[i] =
                        new MoveEntity(move.getCoordinates(), move.getPosition(), move.getPiecePlayed());
                i++;
            }
            gameMovesDao.insertMoves(changed);
        });
    }

    public void deleteGameMoves() {
        service.submit(gameMovesDao::deleteGame);
    }

    public LiveData<String> getMovePosition(String position) {
        return gameMovesDao.getPositionLd(position);
    }

    public LiveData<String> getFirstMove() {
        return gameMovesDao.getFirstMoveLd();
    }

    public Flowable<MoveEntity> getLastMove() {
        return gameMovesDao.getMoveRx();
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

    public List<MoveEntity> getAllMoves() {
//        long start = System.nanoTime();
        Callable<List<MoveEntity>> call = gameMovesDao::getAllMoves;
        Future<List<MoveEntity>> checkAllMoves = service.submit(call);

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
            return gameMovesDao.getPiecePlayed(position);
        }
    }

}
