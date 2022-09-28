package com.esc.test.apps.common.adaptors.move;

import com.esc.test.apps.data.objects.entities.Move;
import com.esc.test.apps.data.objects.pojos.MoveInfo;
import com.esc.test.apps.data.persistence.GamePreferences;
import com.esc.test.apps.data.repositories.FbMoveRepo;
import com.esc.test.apps.data.repositories.implementations.local.GameRepository;
import com.esc.test.apps.data.repositories.implementations.local.MoveRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public class Moves {

    private final GamePreferences gamePref;
    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
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

    public Moves(GameRepository gameRepository, ExecutorService executor, GamePreferences gamePref,
                 MoveRepository moveRepository, FbMoveRepo firebaseMoveRepository,
                 String coordinates, String playedPiece, String moveId, boolean onlineGame
    ) {
        this.gameRepository = gameRepository;
        this.executor = executor;
        this.moveRepository = moveRepository;
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

        moveRepository.insertMove(new Move(tempCube, String.valueOf(cubePos), playedPiece));
        if (onlineGame) firebaseMoveRepository.addMove(
                new MoveInfo(tempCube, String.valueOf(cubePos), playedPiece, moveId, null));

       executor.execute(this::getLinesToCheck);
    }

    private void getLinesToCheck() {
        lines2check.addAll(MoveUtils.addLinesToCheck(cubePos, numCube));
        executor.execute(this::checkOtherCubes);
    }

    private void checkOtherCubes() {
        List<Move> list = moveRepository.getAllMoves();
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
                            index.isPresent() ? list.get(index.getAsInt()).getPiece_played() : null;
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
