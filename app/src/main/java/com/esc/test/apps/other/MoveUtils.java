package com.esc.test.apps.other;

import static com.esc.test.apps.utils.Utils.addNonNull;

import com.esc.test.apps.pojos.CubeID;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MoveUtils {

    public static final int[] TOPLEFT2BACKRIGHT = {0, 21, 42, 63}; //+21
    public static final int[] TOPRIGHT2BACKLEFT = {3, 22, 41, 60}; //+19
    public static final int[] BOTTOMRIGHT2BACKLEFT = {15, 26, 37, 48}; //+9
    public static final int[] BOTTOMLEFT2BACKRIGHT = {12, 25, 38, 51}; //+13

    public static int[] numValue(String cube) {
        int[] numCube = new int[3];
        numCube[0] = Character.getNumericValue(cube.charAt(0));
        numCube[1] = Character.getNumericValue(cube.charAt(1));
        numCube[2] = Character.getNumericValue(cube.charAt(2));
        return numCube;
    }

    public static int getCubePos(int[] numCube) {
        return (numCube[0] * 4) + numCube[1] + (numCube[2] * 16);
    }

    public static String getStringCoord(int pos) {
        int x, y, z;
        z = pos % 16;
        x = (pos - (z * 16)) % 4;
        y = (pos - (z * 16)) - (x * 4);
        return x + "" + y + "" + z;
    }

    public static int getRandomPos(int... exclude) {
        int pos = new Random().nextInt(64);
        if (pos == exclude[0]) getRandomPos(exclude);
        return pos;
    }

    public static int[] generateLine(int floorValue, int increment) {
        int[] values2Check = new int[4];
        for (int i = 0; i < values2Check.length; i++) {
            values2Check[i] = floorValue;
            floorValue += increment;
        }
        return values2Check;
    }

    public static List<int[]> addLinesToCheck(int cubePos, int[] numCube) {
        List<int[]> lines2check = new ArrayList<>();
        addNonNull(lines2check, generateXLine(cubePos));
        addNonNull(lines2check, generateYLine(cubePos, numCube[0]));
        addNonNull(lines2check, generateZLine(cubePos, numCube[2]));
        addNonNull(lines2check, generateXyDiagonal(cubePos, numCube));
        addNonNull(lines2check, generateYzDiagonal(cubePos, numCube));
        addNonNull(lines2check, generateXzDiagonal(cubePos, numCube));
        addNonNull(lines2check, generateXyzDiagonals(cubePos));
        return lines2check;
    }

    private static int[] generateXLine(int cubePos) {
        return generateLine(cubePos - (cubePos % 4), 1);
    }
    private static int[] generateYLine(int cubePos, int xCoord) {
        return generateLine(cubePos - (xCoord * 4), 4);
    }
    private static int[] generateZLine(int cubePos, int zCoord) {
        return generateLine(cubePos - (zCoord * 16), 16);
    }
    private static int[] generateXyDiagonal(int cubePos, int[] numCube) {
        int x = numCube[0], y = numCube[1];
        int xy = x+y;
        if (x == y) return generateLine(cubePos - (x * 5), 5);
        else if (xy == 3) return generateLine(cubePos - (x * 3), 3);
        else return null;
    }
    private static int[] generateYzDiagonal(int cubePos, int[] numCube) {
        int y = numCube[1], z = numCube[2];
        int yz = y+z;
        if (y == z) return generateLine(cubePos - (y * 17), 17);
        else if (yz == 3) return generateLine(cubePos - (z * 15), 15);
        else return null;
    }
    private static int[] generateXzDiagonal(int cubePos, int[] numCube) {
        int z = numCube[2], x = numCube[0];
        int xz = x+z;
        if (x == z) return generateLine(cubePos - (x * 20), 20);
        else if (xz == 3) return generateLine(cubePos - (z * 12), 12);
        else return null;
    }
    private static int[] generateXyzDiagonals(int cubePos) {
        int[] tlbr = checkXyzDiagonal(cubePos, TOPLEFT2BACKRIGHT),
                trlb = checkXyzDiagonal(cubePos, TOPRIGHT2BACKLEFT),
                blbr = checkXyzDiagonal(cubePos, BOTTOMLEFT2BACKRIGHT),
                brbl = checkXyzDiagonal(cubePos, BOTTOMRIGHT2BACKLEFT);
        if (tlbr != null) return tlbr;
        else if (trlb != null) return trlb;
        else if (blbr != null) return blbr;
        else return brbl;
    }
    private static int[] checkXyzDiagonal(int cubePos, int[] xyzDiagonal) {
        boolean contains = false;
        for (int i : xyzDiagonal) {
            if (cubePos == i) {
                contains = true;
                break;
            }
        }
        return contains ? xyzDiagonal : null;
    }

    public static CubeID[] getCubeIds(CubeID[] cubes, int z) {
        int x = 0, y = 0;
        for (int i = 0; i < cubes.length; i++) {
            String cubeName = x + "" + y + "" + z;
            String cubePos = String.valueOf((x * 4) + y + (z * 16));
            if (y == 0 || y == 1 || y == 2) {
                y++;
                cubes[i] = new CubeID(cubeName, cubePos);
            } else {
                cubes[i] = new CubeID(cubeName, cubePos);
                x++;
                y = 0;
            }
        }
        return cubes;
    }
}
