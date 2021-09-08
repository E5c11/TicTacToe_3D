package com.esc.test.apps.other;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.esc.test.apps.datastore.GameState;
import com.esc.test.apps.repositories.FirebaseMoveRepository;
import com.esc.test.apps.repositories.GameRepository;
import com.esc.test.apps.repositories.MoveRepository;

@Singleton
public class MovesFactory {

    private final GameState gameState;
    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    private final FirebaseMoveRepository firebaseMoveRepository;
    private final ResourceProvider rp;

    @Inject
    public MovesFactory(GameState gameState, GameRepository gameRepository, MoveRepository moveRepository, FirebaseMoveRepository firebaseMoveRepository, ResourceProvider rp) {
        this.gameState = gameState;
        this.gameRepository = gameRepository;
        this.moveRepository = moveRepository;
        this.firebaseMoveRepository = firebaseMoveRepository;
        this.rp = rp;
    }

    public Moves createMoves(String coordinates, String playedPiece) {
        return new Moves(gameState, gameRepository,
                moveRepository, firebaseMoveRepository,
                coordinates, playedPiece);
    }
}
