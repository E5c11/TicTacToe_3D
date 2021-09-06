package com.esc.test.apps.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.esc.test.apps.entities.Move;
import com.esc.test.apps.gamestuff.GameMovesDao;

@Singleton
public class MoveRepository {
    private final GameMovesDao gameMovesDao;
    private final LiveData<List<Move>> allMoves;
    private final ExecutorService service;

    @Inject
    public MoveRepository(GameMovesDao gameMovesDao) {
        this.gameMovesDao = gameMovesDao;
        allMoves = gameMovesDao.getAllMoves();
        service = Executors.newFixedThreadPool(2);
    }

    public void insertMove(Move move) {
        service.submit(() -> gameMovesDao.insert(move));
    }

    public void insertMultipleMoves(Move ...moves) {
        service.submit(() -> gameMovesDao.insertMoves(moves));
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

    public LiveData<Move> getLastMove() {
        return gameMovesDao.getMove();
    }

    public String getOccupiedWith(String position) {
        Future<String> checkPieceAtPos = service.submit(new Task(gameMovesDao, position));

        try {return checkPieceAtPos.get(100, TimeUnit.MILLISECONDS);}
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            return null;
        }
    }

    public LiveData<List<Move>> getAllMoves() {
        Log.d("myT", "getAllMoves: ");
        return allMoves;
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
