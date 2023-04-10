package com.esc.test.apps.common.helpers.move;

import com.esc.test.apps.board.moves.data.Move;
import com.esc.test.apps.board.moves.data.MoveEntity;
import com.esc.test.apps.board.moves.data.MoveResponse;
import com.esc.test.apps.board.games.io.GamePreferences;
import com.esc.test.apps.data.repositories.FbMoveRepo;
import com.esc.test.apps.board.games.GameRepository;
import com.esc.test.apps.board.moves.MoveRepositoryLegacy;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public class CheckNewMove {

    private final GamePreferences gamePref;
    private final GameRepository gameRepository;
    private final MoveRepositoryLegacy moveRepositoryLegacy;
    private final FbMoveRepo firebaseMoveRepository;
    private final List<int[]> lines2check = new ArrayList<>();
    private int[] winnerRow = new int[4];
    private int[] numCube;
    private String playedPiece;
    private int numInRow;
    private int cubePos;
    private long start;
    private final boolean onlineGame;
    private static final String TAG = "myT";
    private final ExecutorService executor;

    public CheckNewMove(GameRepository gameRepository, ExecutorService executor, GamePreferences gamePref,
                        MoveRepositoryLegacy moveRepositoryLegacy, FbMoveRepo firebaseMoveRepository,
                        String coordinates, String playedPiece, String moveId, boolean onlineGame
    ) {
        this.gameRepository = gameRepository;
        this.executor = executor;
        this.moveRepositoryLegacy = moveRepositoryLegacy;
        this.firebaseMoveRepository = firebaseMoveRepository;
        this.onlineGame = onlineGame;
        this.gamePref = gamePref;
        executor.execute(() -> findPos(coordinates, playedPiece, moveId));
    }

    private void findPos(String tempCube, String playedPiece, String moveId) {
        numCube = MoveUtils.numValue(tempCube);

        cubePos = MoveUtils.getCubePos(numCube);
        numInRow = 1;
        this.playedPiece = playedPiece;

        moveRepositoryLegacy.insertMove(new Move(tempCube, String.valueOf(cubePos), playedPiece));
        if (onlineGame) firebaseMoveRepository.addMove(
                new MoveResponse(tempCube, String.valueOf(cubePos), playedPiece, moveId, null));

       executor.execute(this::getLinesToCheck);
    }

    private void getLinesToCheck() {
        lines2check.addAll(MoveUtils.addLinesToCheck(cubePos, numCube));
        executor.execute(this::checkOtherCubes);
    }

    private void checkOtherCubes() {
        List<MoveEntity> list = moveRepositoryLegacy.getAllMoves();
        outer : for(int[] line: lines2check) {
            numInRow = 1;
            int i = 0;
            for (int pos : line) {
                if (cubePos != pos && i < list.size()) {
                    i++;
                    OptionalInt index = IntStream.range(0, list.size())
                            .filter(item -> String.valueOf(pos).equals(list.get(item).getPosition()))
                            .findFirst();
                    String posType =
                            index.isPresent() ? list.get(index.getAsInt()).getPiecePlayed() : null;
                    if (posType != null) {
                        if (posType.equals(playedPiece)) {
                            numInRow++;
                            if (numInRow == 4) {
                                winnerRow = line;
                                executor.execute(this::saveWinnerRow);
                                break outer;
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
    }

    private void saveWinnerRow() {
        ArrayList<String> winners = new ArrayList<>();
        for (int i: winnerRow) winners.add(String.valueOf(i));
        gamePref.updateWinnerLineJava(winners);
        gamePref.updateWinnerJava(playedPiece);
        gameRepository.updateWinner(playedPiece);
    }
}
