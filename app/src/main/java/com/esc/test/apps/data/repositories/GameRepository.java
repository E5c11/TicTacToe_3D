package com.esc.test.apps.data.repositories;

import androidx.lifecycle.LiveData;

import com.esc.test.apps.data.objects.entities.Game;
import com.esc.test.apps.data.source.local.GamesDao;
import com.esc.test.apps.common.utils.ExecutorFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Flowable;

@Singleton
public class GameRepository {
    private final GamesDao gamesDao;
    private final LiveData<List<Game>> allGames;
    private final Flowable<String> turn;
    private final Flowable<String> winner;
    private final LiveData<String> starter;
    private static final String TAG = "myT";
    private final ExecutorService service = ExecutorFactory.getFixedSizeExecutor();

    @Inject
    public GameRepository(GamesDao gamesDao) {
        this.gamesDao = gamesDao;
        allGames = gamesDao.getAllGames();
        turn = gamesDao.getTurn();
        winner = gamesDao.getWinner();
        starter = gamesDao.getPreviousStarter();
    }

    public void insertGame(Game game) {
        service.submit(() -> gamesDao.insert(game));
    }

    public void updateWinner(String winner) {
        service.submit(() -> gamesDao.updateWinner(winner));
    }

    public void updateTurn(String turn) {
        service.submit(() -> gamesDao.updateTurn(turn));
    }

    public void setStarter(String starter) {
        service.submit(() -> gamesDao.updateStarter(starter));
    }

    public Flowable<String> getTurn() {
        return turn;
    }

    public Flowable<String> getWinner() {
        return winner;
    }

    public LiveData<String> getPreviousStarter() {
        return starter;
    }

    public LiveData<List<Game>> getAllGames() {
        return allGames;
    }
}