package com.esc.test.apps.other;

import static com.esc.test.apps.other.MoveUtils.NO_MOVES;
import static com.esc.test.apps.other.MoveUtils.addLinesToCheck;
import static com.esc.test.apps.other.MoveUtils.checkAnyMergeCubes;
import static com.esc.test.apps.other.MoveUtils.checkMergeCube;
import static com.esc.test.apps.other.MoveUtils.compareArrayContent;
import static com.esc.test.apps.other.MoveUtils.getRandomCube;
import static com.esc.test.apps.other.MoveUtils.getStringCoord;
import static com.esc.test.apps.other.MoveUtils.numValue;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.esc.test.apps.datastore.UserDetails;
import com.esc.test.apps.entities.Move;
import com.esc.test.apps.utils.ExecutorFactory;
import com.esc.test.apps.utils.Lines;
import com.esc.test.apps.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DifficultMoves {
    //AI lines
    private final List<int[]> possibleLines = new ArrayList<>();
    private final List<int[]> oneCubeLine = new ArrayList<>();
    private final List<int[]> twoCubeLine = new ArrayList<>();
    private final List<int[]> threeCubeLine = new ArrayList<>();
    //user lines
    private final List<int[]> oneLineBlock = new ArrayList<>();
    private final List<int[]> twoLineBlock = new ArrayList<>();
    private final List<int[]> threeLineBlock = new ArrayList<>();

    private List<int[]> openLines = new ArrayList<>();

    private final List<Integer> aICubes = new ArrayList<>();
    private final List<Integer> userCubes = new ArrayList<>();
    private final ExecutorService executor = ExecutorFactory.getSingleExecutor();
    private final SingleLiveEvent<String> error = new SingleLiveEvent<>();
    private final MovesFactory movesFactory;
    private final Random rand;
    private final UserDetails user;
//    private int lastAIMove;
    private int lastUserMove;
    private int moveCount;
    private String aIPiece;
    private String level;
    private static final String TAG = "myT";

    @Inject
    public DifficultMoves(MovesFactory movesFactory, Random rand, UserDetails user) {
        this.movesFactory = movesFactory;
        this.rand = rand;
        this.user = user;
    }

    public void newGame() {
        possibleLines.clear(); oneCubeLine.clear(); twoCubeLine.clear(); threeCubeLine.clear();
        threeLineBlock.clear(); aICubes.clear(); userCubes.clear();
        openLines.clear();
        for (int[] line : Lines.lines) openLines.add(Arrays.copyOf(line, line.length));
        level = user.getLevel();
        Log.d(TAG, "newGame: " + level + " " + openLines.size());
    }

//    public void setFirstMove(int pos, String piece, int count) {
//        lastAIMove = pos;
//        aIPiece = piece;
//        moveCount = count;
//        sendMove(pos);
//        executor.execute(this::createLines);
//    }

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
        userLines.forEach(x -> Log.d(TAG, "userLines: " + Arrays.toString(x)));
        possibleLines.removeAll(userLines);
        oneCubeLine.removeAll(userLines);
        openLines = compareArrayContent(userLines, openLines, true);
        blockUser(userLines);
    }

    private void blockUser(List<int[]> userLine) {
        List<int[]> duplicates = new ArrayList<>(userLine);
        duplicates.retainAll(threeLineBlock);
        //noinspection SuspiciousMethodCalls
        userLine.remove(duplicates);
        userLine.forEach(line -> {
            int cubeInLine = 0;
            for (int i : line) {
                if (aICubes.contains(i)) break;
                if (userCubes.contains(i)) {
//                    Log.d(TAG, "blockUser: " + i);
                    cubeInLine++;
                }
                if (cubeInLine == 1) {
                    oneLineBlock.add(line);
                    openLines = compareArrayContent(Collections.singletonList(line), openLines, true);
                }
                if (cubeInLine == 2) {
                    twoLineBlock.add(line);
                    oneLineBlock.remove(line);
                }
                if (cubeInLine == 3) {
                    threeLineBlock.add(line);
                    twoLineBlock.remove(line);
                    break;
                }
            }
        });
        newMove();
    }

    private void newMove() {
        if (!threeCubeLine.isEmpty()) { //check to win
            Log.d(TAG, "newMove: 3");
            if (level.equals("Normal") || level.equals("Difficult")) chooseMove(threeCubeLine, null);
            else {
                if (threeCubeLine.isEmpty() && threeLineBlock.isEmpty()) checkPossibleMoves();
                else if (!threeCubeLine.isEmpty() && threeCubeLine.size() > 1) chooseMove(threeCubeLine, null);
                else chooseMove(threeCubeLine, (threeLineBlock.isEmpty() ?
                            (twoCubeLine.isEmpty() ? possibleLines : twoCubeLine) : threeLineBlock));
            }
        }
        else if (!threeLineBlock.isEmpty()) { // check to block win
            Log.d(TAG, "newMove: block");
            if (level.equals("Normal") || level.equals("Difficult")) chooseMove(threeLineBlock, null);
            else chooseMove(threeLineBlock, twoCubeLine.isEmpty() ? possibleLines : twoCubeLine);
        }
        else checkPossibleMoves();
    }

    private void checkPossibleMoves() {
        if (possibleLines.isEmpty()) {
            Log.d(TAG, "newMove: anywhere");
            anywhereMove();
        }
        else {
            if (!twoCubeLine.isEmpty()) {
                Log.d(TAG, "newMove: 2");
                    if (level.equals("Normal")) chooseMove(twoCubeLine, null);
                    else if (level.equals("Difficult")) playMergeCube(checkMergeCube(twoCubeLine, aICubes, userCubes));
                    else chooseMove(twoCubeLine, oneCubeLine.isEmpty() ? possibleLines : oneCubeLine);
            }
            else if (!oneCubeLine.isEmpty()) {
                Log.d(TAG, "newMove: 1 ");
                if (level.equals("Normal") || level.equals("Difficult")) {
                    chooseMove(oneCubeLine, null);
                }
                else {
                    if (rand.nextBoolean()) chooseMove(oneCubeLine, null);
                    else anywhereMove();
                }
            } else anywhereMove();
        }
    }

    private int anywhereMove() {
        Log.d(TAG, "anywhereMove: ");
        List<Integer> occupiedCubes = new ArrayList<>();
        occupiedCubes.addAll(aICubes);
        occupiedCubes.addAll(userCubes);
        int newMove = getRandomCube(occupiedCubes);
        if (newMove == NO_MOVES) error.postValue("No moves available");
        sendMove(newMove);
        return newMove;
    }

    private void chooseMove(List<int[]> first, List<int[]> second) {
        if (level.equals("Easy") && second != null) first.addAll(second);
        int[] moveLine = first.get(rand.nextInt(first.size()));
        List<Integer> newPos = new ArrayList<>();
        for (int cube : moveLine) {
            if (!aICubes.contains(cube) && !userCubes.contains(cube))
                newPos.add(cube);
        }
        first.remove(moveLine);
        Log.d(TAG, "chooseMove: ");
        if (newPos.size() != 0) {
            int newMove = randPos(newPos);
            sendMove(newMove);
        }
        else anywhereMove();
    }

    private void playMergeCube(List<Integer> commonCube) {
        Log.d(TAG, "playMergeCube: " + commonCube.toString());
        if (commonCube.isEmpty() && !twoCubeLine.isEmpty()) {
            blockMergeCube(checkMergeCube(twoLineBlock, userCubes, aICubes));
        } else sendMergeCube(commonCube);
    }

    private void blockMergeCube(List<Integer> commonCube) {
        Log.d(TAG, "blockMergeCube: " + commonCube.toString());
        if (commonCube.isEmpty() && !twoLineBlock.isEmpty()) createMergeLines();
        else sendMergeCube(commonCube);
    }

    private void createMergeLines() {
        boolean oneInLine = true;
        List<Integer> possibleCubes = checkAnyMergeCubes(twoCubeLine, aICubes, oneCubeLine);
        if (possibleCubes.isEmpty()) {
            possibleCubes = checkAnyMergeCubes(twoCubeLine, aICubes, openLines);
            oneInLine = false;
        }
        Log.d(TAG, "createMergeLines: " + possibleCubes.toString());
        int mergePos = randPos(possibleCubes);
        List<int[]> mergeLines = addLinesToCheck(mergePos, numValue(getStringCoord(mergePos)));
        if (!oneInLine) mergeLines = compareArrayContent(mergeLines, openLines, false);
        for (int[] line : mergeLines) {
            List<Integer> temp = Arrays.stream(line).boxed().collect(Collectors.toList());
            List<Integer> check = new ArrayList<>(temp);
            List<Integer> possibleMoves = new ArrayList<>(temp);
            check.retainAll(userCubes);
            if (check.isEmpty()) {
                temp.retainAll(aICubes);
                possibleMoves.remove(Integer.valueOf(mergePos));
                if (temp.size() == 1 && oneInLine) {
                    possibleMoves.remove(temp.get(0));
                    Log.d(TAG, "one in line: " + possibleMoves.toString());
                    sendMove(possibleMoves.get(rand.nextBoolean() ? 0 : 1));
                    break;
                } else if (temp.isEmpty() && !oneInLine) {
                    int move = rand.nextInt(3);
                    Log.d(TAG, "zero in line: " + possibleMoves.toString());
                    sendMove(possibleMoves.get(move));
                    break;
                }
            }
        }
    }

    private void addMoveToLines(int move) {
        List<int[]> newLines = addLinesToCheck(move, numValue(getStringCoord(move)));
        arrangeNewLines(newLines);
    }

    private void arrangeNewLines(List<int[]> newLines) {
        List<int[]> remove = new ArrayList<>();
        newLines.forEach(line -> {
            if (oneCubeLine.contains(line) || twoCubeLine.contains(line) || threeCubeLine.contains(line)) {
                remove.add(line);
                Log.d(TAG, "removePossibleLines: ");
            } else {
                int i = 0;
                for (int cube : line) {
                    if (userCubes.contains(cube)) {
                        i = 0;
                        break;
                    } else if (aICubes.contains(cube)) i++;
                }
                if (i == 1) addOneLine(line);
                else if (i == 2) {
                    oneCubeLine.remove(line);
                    addTwoLine(line);
                } else if (i == 3) {
                    twoCubeLine.remove(line);
                    addThreeLine(line);
                }
            }
        });
        removePossibleLines(remove);
    }

//    private void createLines() {
//        List<int[]> newLines = addLinesToCheck(lastAIMove, numValue(getStringCoord(lastAIMove)));
//        possibleLines.addAll(newLines);
//        oneCubeLine.addAll(newLines);
//        aICubes.add(lastAIMove);
//    }

    private void removePossibleLines(List<int[]> list) {
        possibleLines.removeAll(list);
        threeCubeLine.removeAll(list);
        twoCubeLine.removeAll(list);
        oneCubeLine.removeAll(list);
    }

    private void sendMergeCube(List<Integer> commonCube) {
        Log.d(TAG, "sendMergeCube: ");
        if (commonCube.isEmpty()) {
            if (twoCubeLine.isEmpty()) chooseMove(oneCubeLine.isEmpty() ? possibleLines : oneCubeLine, null);
            else createMergeLines();
        } else if (commonCube.size() == 1) sendMove(commonCube.get(0));
        else sendMove(randPos(commonCube));
    }

    private void sendMove(int pos) {
        Log.d(TAG, "sendMove: " + pos);
        aICubes.add(pos);
//        lastAIMove = pos;
        movesFactory.createMoves(getStringCoord(pos), aIPiece,
                String.valueOf(moveCount), false);
        addMoveToLines(pos);
    }

    private void addOneLine(int[] line) {
        possibleLines.add(line);
        oneCubeLine.add(line);
        openLines = compareArrayContent(Collections.singletonList(line), openLines, true);
    }
    private void addTwoLine(int[] line) {
        possibleLines.add(line);
        twoCubeLine.add(line);
    }
    private void addThreeLine(int[] line) {
        possibleLines.add(line);
        threeCubeLine.add(line);
    }

    private int randPos(List<Integer> cubes) {
        if (cubes.isEmpty()) return anywhereMove();
        else return cubes.get(rand.nextInt(cubes.size()));
    }

    public LiveData<String> getError() { return error; }
}
