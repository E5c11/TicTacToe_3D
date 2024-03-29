package com.esc.test.apps.common.helpers.move;

import static com.esc.test.apps.common.utils.Utils.dispose;

import androidx.lifecycle.LiveData;

import com.esc.test.apps.common.utils.SingleLiveEvent;
import com.esc.test.apps.board.moves.data.MoveEntity;
import com.esc.test.apps.data.persistence.UserPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Singleton
public class BotMoveGenerator {
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
    private final SingleLiveEvent<String> error = new SingleLiveEvent<>();
    private final CheckMoveFactory checkMoveFactory;
    private final Random rand;
    private final UserPreferences userPref;
    private Disposable d;
    private int lastUserMove;
    private int moveCount;
    private String aIPiece;
    private String level;

    private static final String TAG = "myT";

    public static final String EASY = "Easy";
    public static final String NORMAL = "Normal";
    public static final String DIFFICULT = "Hard";

    @Inject
    public BotMoveGenerator(CheckMoveFactory checkMoveFactory, Random rand, UserPreferences userPref) {
        this.checkMoveFactory = checkMoveFactory;
        this.rand = rand;
        this.userPref = userPref;
    }

    public void newGame() {
        possibleLines.clear(); oneCubeLine.clear(); twoCubeLine.clear(); threeCubeLine.clear();
        threeLineBlock.clear(); aICubes.clear(); userCubes.clear();
        openLines.clear();
        for (int[] line : Lines.lines) openLines.add(Arrays.copyOf(line, line.length));
        d = userPref.getUserPreference().subscribeOn(Schedulers.io()).doOnNext( pref -> {
            level = pref.getLevel();
            dispose(d);
        }).subscribe();
    }

    public void setPiece(String piece, int mCount) {
        aIPiece = piece;
        moveCount = mCount;
        newGame();
    }

    public void eliminateLines(MoveEntity moveEntity) throws Exception {
        int userPos = Integer.parseInt(moveEntity.getPosition());
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

    private void userLines() throws Exception {
        List<int[]> userLines = MoveUtils.addLinesToCheck(lastUserMove, MoveUtils.numValue(MoveUtils.getStringCoord(lastUserMove)));
        possibleLines.removeAll(userLines);
        oneCubeLine.removeAll(userLines);
        openLines = MoveUtils.compareArrayContent(userLines, openLines, true);
        blockUser(userLines);
    }

    private void blockUser(List<int[]> userLine) throws Exception {
        List<int[]> duplicates = new ArrayList<>(userLine);
        duplicates.retainAll(threeLineBlock);
        //noinspection SuspiciousMethodCalls
        userLine.remove(duplicates);
        userLine.forEach(line -> {
            int cubeInLine = 0;
            for (int i : line) {
                if (aICubes.contains(i)) break;
                if (userCubes.contains(i)) {
                    cubeInLine++;
                }
                if (cubeInLine == 1) {
                    oneLineBlock.add(line);
                    openLines = MoveUtils.compareArrayContent(Collections.singletonList(line), openLines, true);
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

    private void newMove() throws Exception {
        if (!threeCubeLine.isEmpty()) { //check to win
            if (level.equals(NORMAL) || level.equals(DIFFICULT)) chooseMove(threeCubeLine, null);
            else {
                if (threeCubeLine.isEmpty() && threeLineBlock.isEmpty()) checkPossibleMoves();
                else if (!threeCubeLine.isEmpty() && threeCubeLine.size() > 1) chooseMove(threeCubeLine, null);
                else chooseMove(threeCubeLine, (threeLineBlock.isEmpty() ?
                            (twoCubeLine.isEmpty() ? possibleLines : twoCubeLine) : threeLineBlock));
            }
        }
        else if (!threeLineBlock.isEmpty()) { // check to block win
            if (level.equals(NORMAL) || level.equals(DIFFICULT)) chooseMove(threeLineBlock, null);
            else chooseMove(threeLineBlock, twoCubeLine.isEmpty() ? possibleLines : twoCubeLine);
        }
        else checkPossibleMoves();
    }

    private void checkPossibleMoves() throws Exception {
        if (possibleLines.isEmpty()) anywhereMove();
        else {
            if (!twoCubeLine.isEmpty()) {
                if (level.equals(NORMAL)) chooseMove(twoCubeLine, null);
                else if (level.equals(DIFFICULT)) playMergeCube(MoveUtils.checkMergeCube(twoCubeLine, aICubes, userCubes));
                else chooseMove(twoCubeLine, oneCubeLine.isEmpty() ? possibleLines : oneCubeLine);
            }
            else if (!oneCubeLine.isEmpty()) {
                if (level.equals(NORMAL) || level.equals(DIFFICULT)) chooseMove(oneCubeLine, null);
                else {
                    if (rand.nextBoolean()) chooseMove(oneCubeLine, null);
                    else anywhereMove();
                }
            } else anywhereMove();
        }
    }

    private int anywhereMove() throws Exception {
        List<Integer> occupiedCubes = new ArrayList<>();
        occupiedCubes.addAll(aICubes);
        occupiedCubes.addAll(userCubes);
        int newMove = MoveUtils.getRandomCube(occupiedCubes);
        if (newMove == MoveUtils.NO_MOVES) throw new Exception("No moves available");
        sendMove(newMove);
        return newMove;
    }

    private void chooseMove(List<int[]> first, List<int[]> second) throws Exception {
        if (level.equals(EASY) && second != null) first.addAll(second);
        int[] moveLine = first.get(rand.nextInt(first.size()));
        List<Integer> newPos = new ArrayList<>();
        for (int cube : moveLine) {
            if (!aICubes.contains(cube) && !userCubes.contains(cube))
                newPos.add(cube);
        }
        first.remove(moveLine);
        if (newPos.size() != 0) {
            int newMove = randPos(newPos);
            sendMove(newMove);
        }
        else anywhereMove();
    }

    private void playMergeCube(List<Integer> commonCube) throws Exception {
        if (commonCube.isEmpty() && !twoCubeLine.isEmpty()) {
            blockMergeCube(MoveUtils.checkMergeCube(twoLineBlock, userCubes, aICubes));
        } else sendMergeCube(commonCube);
    }

    private void blockMergeCube(List<Integer> commonCube) throws Exception {
        if (commonCube.isEmpty() && !twoLineBlock.isEmpty()) createMergeLines();
        else sendMergeCube(commonCube);
    }

    private void createMergeLines() throws Exception {
        boolean oneInLine = true;
        List<Integer> possibleCubes = MoveUtils.checkAnyMergeCubes(twoCubeLine, aICubes, oneCubeLine);
        if (possibleCubes.isEmpty()) {
            possibleCubes = MoveUtils.checkAnyMergeCubes(twoCubeLine, aICubes, openLines);
            oneInLine = false;
        }
        int mergePos = randPos(possibleCubes);
        List<int[]> mergeLines = MoveUtils.addLinesToCheck(mergePos, MoveUtils.numValue(MoveUtils.getStringCoord(mergePos)));
        if (!oneInLine) mergeLines = MoveUtils.compareArrayContent(mergeLines, openLines, false);
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
                    sendMove(possibleMoves.get(rand.nextBoolean() ? 0 : 1));
                    break;
                } else if (temp.isEmpty() && !oneInLine) {
                    int move = rand.nextInt(3);
                    sendMove(possibleMoves.get(move));
                    break;
                }
            }
        }
    }

    private void addMoveToLines(int move) {
        List<int[]> newLines = MoveUtils.addLinesToCheck(move, MoveUtils.numValue(MoveUtils.getStringCoord(move)));
        arrangeNewLines(newLines);
    }

    private void arrangeNewLines(List<int[]> newLines) {
        List<int[]> remove = new ArrayList<>();
        newLines.forEach(line -> {
            if (oneCubeLine.contains(line) || twoCubeLine.contains(line) || threeCubeLine.contains(line)) {
                remove.add(line);
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

    private void removePossibleLines(List<int[]> list) {
        possibleLines.removeAll(list);
        threeCubeLine.removeAll(list);
        twoCubeLine.removeAll(list);
        oneCubeLine.removeAll(list);
    }

    private void sendMergeCube(List<Integer> commonCube) throws Exception {
        if (commonCube.isEmpty()) {
            if (twoCubeLine.isEmpty()) chooseMove(oneCubeLine.isEmpty() ? possibleLines : oneCubeLine, null);
            else createMergeLines();
        } else if (commonCube.size() == 1) sendMove(commonCube.get(0));
        else sendMove(randPos(commonCube));
    }

    private void sendMove(int pos) {
        aICubes.add(pos);
        checkMoveFactory.createMoves(MoveUtils.getStringCoord(pos), aIPiece,
                String.valueOf(moveCount), false);
        addMoveToLines(pos);
    }

    private void addOneLine(int[] line) {
        possibleLines.add(line);
        oneCubeLine.add(line);
        openLines = MoveUtils.compareArrayContent(Collections.singletonList(line), openLines, true);
    }
    private void addTwoLine(int[] line) {
        possibleLines.add(line);
        twoCubeLine.add(line);
    }
    private void addThreeLine(int[] line) {
        possibleLines.add(line);
        threeCubeLine.add(line);
    }

    private int randPos(List<Integer> cubes) throws Exception {
        if (cubes.isEmpty()) return anywhereMove();
        else return cubes.get(rand.nextInt(cubes.size()));
    }

    public LiveData<String> getError() { return error; }
}
