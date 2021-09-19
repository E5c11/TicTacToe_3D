package com.esc.test.apps.other;

import static com.esc.test.apps.other.MoveUtils.addLinesToCheck;
import static com.esc.test.apps.other.MoveUtils.getStringCoord;
import static com.esc.test.apps.other.MoveUtils.numValue;

import com.esc.test.apps.entities.Move;
import com.esc.test.apps.utils.ExecutorFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AIMoves {

    private final List<int[]> possibleLines = new ArrayList<>();
    private final List<int[]> lines2block = new ArrayList<>();
    private final List<Integer> aICubes = new ArrayList<>();
    private final List<Integer> userCubes = new ArrayList<>();
    private final ExecutorService executor = ExecutorFactory.getSingleExecutor();
    private final MovesFactory movesFactory;
    private final Random rand;
    private int lastAIMove;
    private int moveCount;
    private String aIPiece;

    @Inject
    public AIMoves(MovesFactory movesFactory, Random rand) {
        this.movesFactory = movesFactory;
        this.rand = rand;
    }

    public void setFirstMove(int pos, String piece, int count) {
        lastAIMove = pos;
        aIPiece = piece;
        moveCount = count;
        sendMove(pos);
        executor.execute(this::createLines);
    }

    public void newMove() {
        if (lines2block.isEmpty()) {
            if (possibleLines.isEmpty()) anywhereMove();
            else chooseMove(possibleLines, aICubes); // check most cubes
        } else chooseMove(lines2block, userCubes);
    }

    private void anywhereMove() {
        List<Integer> occupiedCubes = new ArrayList<>();
        occupiedCubes.addAll(aICubes);
        occupiedCubes.addAll(userCubes);
        sendMove(getRandomCube(0, 64, occupiedCubes));
    }

    public int getRandomCube(int start, int end, List<Integer> exclude) {
        int random = start + rand.nextInt(end - start + 1 - exclude.size());
        for (int ex : exclude) {
            if (random < ex) break;
            random++;
        }
        return random;
    }

    private void chooseMove(List<int[]> lines, List<Integer> cubes) {
        int randomLine = rand.nextInt(lines.size());
        int[] moveLine = lines.get(randomLine); // check which line has the most occupied cubes
        List<Integer> newPos = new ArrayList<>();
        for (int cube : moveLine) {
            if (!cubes.contains(cube)) newPos.add(cube);
        }
        sendMove(newPos.get(rand.nextInt(newPos.size())));
    }

    private void createLines() {
        possibleLines.addAll(addLinesToCheck(lastAIMove, numValue(getStringCoord(lastAIMove))));
        aICubes.add(lastAIMove);
    }

    public void eliminateLines(Move move) {
        int userPos = Integer.parseInt(move.getPosition());
        possibleLines.forEach(line -> {
            if (Arrays.stream(line).anyMatch(pos -> pos == userPos))
                possibleLines.remove(line);
        });
        userCubes.add(userPos);
        newMove();
    }

    private void sendMove(int pos) {
        movesFactory.createMoves(getStringCoord(pos), aIPiece,
                String.valueOf(moveCount), false);
    }

    public void blockUser(int[] line) {
        lines2block.add(line);
    }
}
