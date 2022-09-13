package com.esc.test.apps.common.adaptors.move;

import com.esc.test.apps.data.persistence.GamePreferences;
import com.esc.test.apps.data.repositories.implementations.remote.FirebaseMoveRepository;
import com.esc.test.apps.data.repositories.implementations.local.GameRepository;
import com.esc.test.apps.data.repositories.implementations.local.MoveRepository;
import com.esc.test.apps.common.utils.ExecutorFactory;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MovesFactory {

    private final ExecutorService executor = ExecutorFactory.getSingleExecutor();
    private final GamePreferences gamePref;
    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    private final FirebaseMoveRepository firebaseMoveRepository;

    @Inject
    public MovesFactory(GameRepository gameRepository, MoveRepository moveRepository,
                        FirebaseMoveRepository firebaseMoveRepository, GamePreferences gamePref
    ) {
        this.gameRepository = gameRepository;
        this.moveRepository = moveRepository;
        this.firebaseMoveRepository = firebaseMoveRepository;
        this.gamePref = gamePref;
    }

    public void createMoves(String coordinates, String playedPiece, String moveId, boolean onlineGame) {
        new Moves(gameRepository, executor, gamePref, moveRepository, firebaseMoveRepository,
                coordinates, playedPiece, moveId, onlineGame);
    }
}
