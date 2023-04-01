package com.esc.test.apps.common.helpers.move;

import com.esc.test.apps.board.moves.MoveRepositoryLegacy;
import com.esc.test.apps.common.utils.ExecutorFactory;
import com.esc.test.apps.data.persistence.GamePreferences;
import com.esc.test.apps.data.repositories.FbMoveRepo;
import com.esc.test.apps.data.repositories.implementations.local.GameRepository;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CheckMoveFactory {

    private final ExecutorService executor = ExecutorFactory.getSingleExecutor();
    private final GamePreferences gamePref;
    private final GameRepository gameRepository;
    private final MoveRepositoryLegacy moveRepositoryLegacy;
    private final FbMoveRepo firebaseMoveRepository;

    @Inject
    public CheckMoveFactory(GameRepository gameRepository, MoveRepositoryLegacy moveRepositoryLegacy,
                            FbMoveRepo firebaseMoveRepository, GamePreferences gamePref
    ) {
        this.gameRepository = gameRepository;
        this.moveRepositoryLegacy = moveRepositoryLegacy;
        this.firebaseMoveRepository = firebaseMoveRepository;
        this.gamePref = gamePref;
    }

    public void createMoves(String coordinates, String playedPiece, String moveId, boolean onlineGame) {
        new CheckNewMove(gameRepository, executor, gamePref, moveRepositoryLegacy, firebaseMoveRepository,
                coordinates, playedPiece, moveId, onlineGame);
    }
}
