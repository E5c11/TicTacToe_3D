package com.esc.test.apps.other;

import android.util.Log;

import com.esc.test.apps.datastore.GameState;
import com.esc.test.apps.entities.Move;
import com.esc.test.apps.pojos.MoveInfo;
import com.esc.test.apps.repositories.FirebaseMoveRepository;
import com.esc.test.apps.repositories.GameRepository;
import com.esc.test.apps.repositories.MoveRepository;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Moves {

    private final GameState gameState;
    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    private final FirebaseMoveRepository firebaseMoveRepository;
    private int cubePos;
    private final ArrayList<int[]> lines2check = new ArrayList<>();
    private int[] winnerRow = new int[4];
    private final int[] numCube = new int[3];
    private String playedPiece;
    private int numInRow;
    private static final String TAG = "myT";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Moves(GameState gameState, GameRepository gameRepository,
                 MoveRepository moveRepository, FirebaseMoveRepository firebaseMoveRepository,
                 String coordinates, String playedPiece, String moveId, boolean myTurn
    ) {
        this.gameState = gameState;
        this.gameRepository = gameRepository;
        this.moveRepository = moveRepository;
        this.firebaseMoveRepository = firebaseMoveRepository;
        executor.execute(() -> findPos(coordinates, playedPiece, moveId, myTurn));
    }

    private String getPosType(String linePos) { return moveRepository.getOccupiedWith(linePos);}

    private void checkOtherCubes() {
        for(int[] line: lines2check) {
            //Log.d("myT", Arrays.toString(line));
            numInRow = 1;
            for (int i : line) {
                //Log.d("myT", "" + i);
                if (cubePos != i) {
                    //Log.d("myT", cubePos + " " + i);
                    String posType = getPosType(String.valueOf(i));
                    //Log.d("myT", "position " + posType);
                    if (posType != null) {
                        //Log.d("myT", "position " + posType);
                        if (posType.equals(playedPiece)) {
                            numInRow++;
                            Log.d("myT", "pieces in row " + numInRow);
                            if (numInRow == 4) {
                                winnerRow = line;
                                executor.execute(this::saveWinnerRow);
                                break;
                            }
                        }
                        else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
    }

    private void UDvalues() {
        lines2check.add(generateLine(cubePos - (numCube[0] * 4), 4));
        executor.execute(this::LRvalues);
    }

    private void LRvalues() {
        lines2check.add(generateLine(cubePos - (cubePos % 4), 1));
        executor.execute(this::FBvalues);
    }

    private void FBvalues() {
        lines2check.add(generateLine(cubePos - (numCube[2] * 16), 16));
        executor.execute(this::xyDiagonal);
    }

    private void xyDiagonal() {
        int x = numCube[0], y = numCube[1];
        int xy = x+y;
        if (x == y) lines2check.add(generateLine(cubePos - (x * 5), 5));
        else if (xy == 3)lines2check.add(generateLine(cubePos - (x * 3), 3));
        executor.execute(this::yzDiagonal);
    }

    private void yzDiagonal() {
        int y = numCube[1], z = numCube[2];
        int yz = y+z;
        if (y == z) lines2check.add(generateLine(cubePos - (y * 17), 17));
        else if (yz == 3) lines2check.add(generateLine(cubePos - (z * 15), 15));
        xzDiagonal();
    }

    private void xzDiagonal() {
        int z = numCube[2], x = numCube[0];
        int xz = x+z;
        if (x == z) lines2check.add(generateLine(cubePos - (x * 20), 20));
        else if (xz == 3) lines2check.add(generateLine(cubePos - (z * 12), 12));
        executor.execute(this::xyzDiagonals);
    }

    private void xyzDiagonals() {
        //int z = numCube[2], x = numCube[0], y = numCube[1];
        if (TopLeft2backRight(cubePos) != null) lines2check.add(TopLeft2backRight(cubePos));
        if (TopRight2backLeft(cubePos) != null) lines2check.add(TopRight2backLeft(cubePos));
        if (BottomLeft2backRight(cubePos) != null) lines2check.add(BottomLeft2backRight(cubePos));
        if (BottomRight2backLeft(cubePos) != null) lines2check.add(BottomRight2backLeft(cubePos));
        executor.execute(this::checkOtherCubes);
    }

    private int[] generateLine(int floorValue, int increment) {
        int[] values2Check = new int[4];
        for (int i = 0; i < values2Check.length; i++) {
            values2Check[i] = floorValue;
            //Log.d("myT", floorValue + "");
            floorValue += increment;
        }
        //Log.d("myT", Arrays.toString(values2Check));
        return values2Check;
    }

    public void findPos(String tempCube, String playedPiece, String moveId, boolean myTurn) {
        Log.d(TAG, tempCube + " " + tempCube.length());
        String[] cube = {String.valueOf(tempCube.charAt(0)), String.valueOf(tempCube.charAt(1)), String.valueOf(tempCube.charAt(2))};
        numValue(cube);
        cubePos = (numCube[0] * 4) + numCube[1] + (numCube[2] * 16);
        //Log.d("myT", "pos value " + cubePos);
        numInRow = 1;
        this.playedPiece = playedPiece;
        moveRepository.insertMove(new Move(tempCube, String.valueOf(cubePos), playedPiece));
        if (myTurn) {
//            Log.d(TAG, "findPos: uploading move " + cubePos);
            firebaseMoveRepository.addMove(new MoveInfo(tempCube, String.valueOf(cubePos), playedPiece, moveId, null));
        }
//        Log.d("myT", "co: " + tempCube + " pos: " + cubePos + " piece played: " + playedPiece);
        lines2check.clear();
        executor.execute(this::UDvalues);
    }

    private void numValue(String[] cube) {
        numCube[0] = Integer.parseInt(cube[0]);
        numCube[1] = Integer.parseInt(cube[1]);
        numCube[2] = Integer.parseInt(cube[2]);
    }

    private int[] TopLeft2backRight(int pos) {
        int[] topLeft2backRight = {0, 21, 42, 63}; //+21
        boolean contains = false;
        for (int i : topLeft2backRight) {
            if (pos == i) {
                contains = true;
                break;
            }
        }
        if (contains) return topLeft2backRight;
        else return null;
    }

    private int[] TopRight2backLeft(int pos) {
        int[] topRight2backLeft = {3, 22, 41, 60}; //+21
        boolean contains = false;
        for (int i : topRight2backLeft) {
            if (pos == i) {
                contains = true;
                break;
            }
        }
        if (contains) return topRight2backLeft;
        else return null;
    }

    private int[] BottomRight2backLeft(int pos) {
        int[] bottomRight2backLeft = {15, 26, 37, 48}; //+21
        boolean contains = false;
        for (int i : bottomRight2backLeft) {
            if (pos == i) {
                contains = true;
                break;
            }
        }
        if (contains) return bottomRight2backLeft;
        else return null;
    }

    private int[] BottomLeft2backRight(int pos) {
        int[] bottomLeft2backRight = {12, 25, 38, 51}; //+21
        boolean contains = false;
        for (int i : bottomLeft2backRight) {
            if (pos == i) {
                contains = true;
                break;
            }
        }
        if (contains) return bottomLeft2backRight;
        else return null;
    }

    private void saveWinnerRow() {
        ArrayList<String> winners = new ArrayList<>();
        Log.d(TAG, "game won");
        for (int i: winnerRow) {
            winners.add(String.valueOf(i));
        }
        gameState.setWinner(playedPiece);
        gameState.setWinnerLine(winners);
        gameRepository.updateWinner(playedPiece);
    }

}
