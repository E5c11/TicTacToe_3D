package com.esc.test.apps.other;

import static com.esc.test.apps.other.MoveUtils.addLinesToCheck;
import static com.esc.test.apps.other.MoveUtils.getStringCoord;
import static com.esc.test.apps.other.MoveUtils.numValue;

import android.util.Log;

import com.esc.test.apps.entities.Move;
import com.esc.test.apps.utils.ExecutorFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AIMoves {

    private final List<int[]> possibleLines = new ArrayList<>();
    private final List<int[]> oneCubeLine = new ArrayList<>();
    private final List<int[]> twoCubeLine = new ArrayList<>();
    private final List<int[]> threeCubeLine = new ArrayList<>();
    private final List<int[]> lines2block = new ArrayList<>();
    private final List<Integer> aICubes = new ArrayList<>();
    private final List<Integer> userCubes = new ArrayList<>();
    private final ExecutorService executor = ExecutorFactory.getSingleExecutor();
    private final MovesFactory movesFactory;
    private final Random rand;
    private int lastAIMove;
    private int lastUserMove;
    private int moveCount;
    private String aIPiece;
    private static final String TAG = "myT";

    @Inject
    public AIMoves(MovesFactory movesFactory, Random rand) {
        this.movesFactory = movesFactory;
        this.rand = rand;
    }

    public void newGame() {
        possibleLines.clear();
        oneCubeLine.clear();
        twoCubeLine.clear();
        threeCubeLine.clear();
        lines2block.clear();
        aICubes.clear();
        userCubes.clear();
    }

    public void setFirstMove(int pos, String piece, int count) {
        lastAIMove = pos;
        aIPiece = piece;
        moveCount = count;
        sendMove(pos);
        executor.execute(this::createLines);
    }

    public void setPiece(String piece, int mCount) {
        aIPiece = piece;
        moveCount = mCount;
    }

    public void eliminateLines(Move move) {
        int userPos = Integer.parseInt(move.getPosition());
        List<int[]> remove = new ArrayList<>();
        possibleLines.forEach(line -> {
            if (Arrays.stream(line).anyMatch(pos -> pos == userPos))
                remove.add(line);
        });
        userCubes.add(userPos);
        lastUserMove = userPos;
        removePossibleLines(remove);
        userLines();
    }

    private void userLines() {
        List<int[]> userLines = addLinesToCheck(lastUserMove, numValue(getStringCoord(lastUserMove)));
        possibleLines.removeAll(userLines);
        oneCubeLine.removeAll(userLines);
//        Log.d(TAG, "userLines: " + Thread.currentThread().getName());
        blockUser(userLines);
    }

    public void blockUser(List<int[]> userLine) {
        List<int[]> duplicates = new ArrayList<>(userLine);
        duplicates.retainAll(lines2block);
        //noinspection SuspiciousMethodCalls
        userLine.remove(duplicates);
        userLine.forEach(line -> {
            int cubeInLine = 0;
            int count = 0;
            for (int i : line) {
                count++;
                if (aICubes.contains(i)) break;
                if (userCubes.contains(i)) cubeInLine++;
                else if (count > 1 && cubeInLine < 3) break;
                if (cubeInLine == 3) {
                    lines2block.add(line);
                    break;
                }
            }

        });
        newMove();
    }

    public void newMove() {
        if (!threeCubeLine.isEmpty()) chooseMove(threeCubeLine);
        else if (!lines2block.isEmpty()) chooseMove(lines2block);
        else {
            Log.d(TAG, "newMove: 3");
            if (possibleLines.isEmpty()) anywhereMove();
            else {
                if (!twoCubeLine.isEmpty()) {
                    Log.d(TAG, "newMove: 2");
                    chooseMove(twoCubeLine);
                }
                else {
                    Log.d(TAG, "newMove: 1");
                    chooseMove(oneCubeLine);
                }
            }
        }
    }

    private void anywhereMove() {
        Log.d(TAG, "anywhereMove: ");
        List<Integer> occupiedCubes = new ArrayList<>();
        occupiedCubes.addAll(aICubes);
        occupiedCubes.addAll(userCubes);
        int newMove = getRandomCube(occupiedCubes);
        aICubes.add(newMove);
        sendMove(newMove);
        addMoveToLines(newMove);
    }

    private void chooseMove(List<int[]> lines) {
        int randomLine = rand.nextInt(lines.size());
        int[] moveLine = lines.get(randomLine);
        List<Integer> newPos = new ArrayList<>();
        for (int cube : moveLine) {
            if (!aICubes.contains(cube) && !userCubes.contains(cube))
                newPos.add(cube);

        }
        lines.remove(moveLine);
        Log.d(TAG, "chooseMove: " + newPos.size());
        if (newPos.size() != 0) {
            int newMove = newPos.get(rand.nextInt(newPos.size()));
            aICubes.add(newMove);
            sendMove(newMove);
            addMoveToLines(newMove);
        }
    }

    private void addMoveToLines(int move) {
        List<int[]> newLines = addLinesToCheck(move, numValue(getStringCoord(move)));
        arrangeNewLines(newLines);
    }

    private void arrangeNewLines(List<int[]> newLines) {
        List<int[]> remove = new ArrayList<>();
        newLines.forEach(line -> {
            if (oneCubeLine.contains(line) || twoCubeLine.contains(line) || threeCubeLine.contains(line))
                remove.add(line);
            else {
                int i = 0;
                for (int cube : line) {
                    if (userCubes.contains(cube)) {
                        i = 0;
                        break;
                    } else if (aICubes.contains(cube)) i++;
                }
                if (i == 1) addOneLine(line);
                else if (i == 2) addTwoLine(line);
                else if (i == 3) addThreeLine(line);
            }
        });
        removePossibleLines(remove);
    }

    private static int getRandomCube(List<Integer> exclude) {
        int[] range = IntStream.rangeClosed(0, 63).toArray();
        List<Integer> rangeExcluding = Arrays.stream(range).boxed().collect(Collectors.toList());
        rangeExcluding.removeAll(exclude);
        return rangeExcluding.get(new Random().nextInt(rangeExcluding.size()));
    }

    private void createLines() {
        List<int[]> newLines = addLinesToCheck(lastAIMove, numValue(getStringCoord(lastAIMove)));
        possibleLines.addAll(newLines);
        oneCubeLine.addAll(newLines);
        aICubes.add(lastAIMove);
    }

    private void removePossibleLines(List<int[]> list) {
        possibleLines.removeAll(list);
        threeCubeLine.removeAll(list);
        twoCubeLine.removeAll(list);
        oneCubeLine.removeAll(list);
    }

    private void sendMove(int pos) {
        Log.d(TAG, "sendMove: " + pos);
        movesFactory.createMoves(getStringCoord(pos), aIPiece,
                String.valueOf(moveCount), false);
    }

    public void addOneLine(int[] line) {
        possibleLines.add(line);
        oneCubeLine.add(line);
    }
    public void addTwoLine(int[] line) {
        possibleLines.add(line);
        twoCubeLine.add(line);
    }
    public void addThreeLine(int[] line) {
        possibleLines.add(line);
        threeCubeLine.add(line);
    }
}
