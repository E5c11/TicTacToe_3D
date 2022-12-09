package com.esc.test.apps.data.source.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import com.esc.test.apps.data.models.entities.Game;

import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface GamesDao {

    @Insert
    void insert(Game game);

    //@Query("DELETE FROM game_moves_table")
    //void deleteGame();

    @Query("UPDATE games_table SET winner = :winner WHERE id = (SELECT MAX(id) FROM games_table)")
    void updateWinner(String winner);

    @Query("UPDATE games_table SET turn = :turn WHERE id = (SELECT MAX(id) FROM games_table)")
    void updateTurn(String turn);

    @Query("UPDATE games_table SET starter = :starter WHERE id = (SELECT MAX(id) FROM games_table)")
    void updateStarter(String starter);

    @Query("SELECT starter FROM games_table ORDER BY `id` DESC LIMIT 2")
    LiveData<String> getPreviousStarter();

    @Query("SELECT turn FROM games_table ORDER BY `id` DESC LIMIT 1")
    Flowable<String> getTurn();

    @Query("SELECT winner FROM games_table ORDER BY `id` DESC LIMIT 1")
    Flowable<String> getWinner();

    @Query("SELECT * FROM games_table")
    LiveData<List<Game>> getAllGames();
}
