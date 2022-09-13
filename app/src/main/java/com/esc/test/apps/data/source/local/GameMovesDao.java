package com.esc.test.apps.data.source.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import com.esc.test.apps.data.objects.entities.Move;

import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface GameMovesDao {

    @Insert
    void insert(Move move);

    @Insert
    void insertMoves(Move...moves);

    @Query("DELETE FROM game_moves_table")
    void deleteGame();

    @Query("SELECT position FROM game_moves_table WHERE position = :position")
    LiveData<String> getPosition(String position);

    @Query("SELECT piece_played FROM game_moves_table WHERE position = :position")
    String getPiece_played(String position);

    @Query("SELECT * FROM game_moves_table ORDER BY position ASC")
    List<Move> getAllMoves();

    @Query("SELECT * FROM game_moves_table ORDER BY id DESC LIMIT 1")
    Flowable<Move> getMove();

    @Query("SELECT piece_played FROM game_moves_table WHERE id = 1")
    LiveData<String> getFirstMove();
}
