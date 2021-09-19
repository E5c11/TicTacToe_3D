package com.esc.test.apps.other;

import static com.esc.test.apps.other.MoveUtils.addLinesToCheck;
import static com.esc.test.apps.other.MoveUtils.getCubePos;
import static com.esc.test.apps.other.MoveUtils.numValue;

import android.util.Log;

import com.esc.test.apps.datastore.GameState;
import com.esc.test.apps.entities.Move;
import com.esc.test.apps.pojos.MoveInfo;
import com.esc.test.apps.repositories.FirebaseMoveRepository;
import com.esc.test.apps.repositories.GameRepository;
import com.esc.test.apps.repositories.MoveRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public class Moves {

    private final GameState gameState;
    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    private final FirebaseMoveRepository firebaseMoveRepository;
    private final AIMoves aiMoves;
    private final List<int[]> lines2check = new ArrayList<>();
    private int[] winnerRow = new int[4];
    private int[] numCube;
    private String playedPiece;
    private int numInRow;
    private int cubePos;
    private long start;
    private boolean myTurn;
    private static final String TAG = "myT";
    private final ExecutorService executor;

    public Moves(GameState gameState, GameRepository gameRepository, ExecutorService executor,
                 MoveRepository moveRepository, FirebaseMoveRepository firebaseMoveRepository,
                 String coordinates, String playedPiece, String moveId, boolean myTurn, AIMoves aiMoves
    ) {
        this.gameState = gameState;
        this.gameRepository = gameRepository;
        this.executor = executor;
        this.moveRepository = moveRepository;
        this.firebaseMoveRepository = firebaseMoveRepository;
        this.myTurn = myTurn;
        this.aiMoves = aiMoves;
        executor.execute(() -> findPos(coordinates, playedPiece, moveId, myTurn));
    }

    private void findPos(String tempCube, String playedPiece, String moveId, boolean myTurn) {
        Log.d(TAG, tempCube + " " + tempCube.length());
        start = System.nanoTime();
        numCube = numValue(tempCube);

        cubePos = getCubePos(numCube);
        numInRow = 1;
        this.playedPiece = playedPiece;

        moveRepository.insertMove(new Move(tempCube, String.valueOf(cubePos), playedPiece));
        if (myTurn) firebaseMoveRepository.addMove(
                new MoveInfo(tempCube, String.valueOf(cubePos), playedPiece, moveId, null));

       executor.execute(this::getLinesToCheck);
    }

    private void getLinesToCheck() {
        lines2check.addAll(addLinesToCheck(cubePos, numCube));
        executor.execute(this::checkOtherCubes);
    }

    private void checkOtherCubes() {
        Log.d(TAG, "before check: " + (System.nanoTime() - start) + " ns");
        List<Move> list = moveRepository.getAllMoves();
        outer : for(int[] line: lines2check) {
            Log.d(TAG, "checkOtherCubes: " + Arrays.toString(line));
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
                            if (numInRow == 3 && myTurn) aiMoves.blockUser(line);
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
        Log.d(TAG, "game won");
        for (int i: winnerRow) winners.add(String.valueOf(i));
        Log.d(TAG, "time: " + ((System.nanoTime() - start) / 1000000) + "ms");
        gameState.setWinner(playedPiece);
        gameState.setWinnerLine(winners);
        gameRepository.updateWinner(playedPiece);
    }
}
