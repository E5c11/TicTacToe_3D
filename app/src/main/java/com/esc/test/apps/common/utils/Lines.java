package com.esc.test.apps.common.utils;

import static com.esc.test.apps.common.adaptors.move.MoveUtils.BOTTOMLEFT2BACKRIGHT;
import static com.esc.test.apps.common.adaptors.move.MoveUtils.BOTTOMRIGHT2BACKLEFT;
import static com.esc.test.apps.common.adaptors.move.MoveUtils.TOPLEFT2BACKRIGHT;
import static com.esc.test.apps.common.adaptors.move.MoveUtils.TOPRIGHT2BACKLEFT;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Lines {

    public static final List<int[]> lines = new ArrayList<>() {{

            //x-lines
            add(new int[]{0, 1, 2, 3});
            add(new int[]{4, 5, 6, 7});
            add(new int[]{8, 9, 10, 11});
            add(new int[]{12, 13, 14, 15});

            add(new int[]{16, 17, 18, 19});
            add(new int[]{20, 21, 22, 23});
            add(new int[]{24, 25, 26, 27});
            add(new int[]{28, 29, 30, 31});

            add(new int[]{32, 33, 34, 35});
            add(new int[]{36, 37, 38, 39});
            add(new int[]{40, 41, 42, 43});
            add(new int[]{44, 45, 46, 47});

            add(new int[]{48, 49, 50, 51});
            add(new int[]{52, 53, 54, 55});
            add(new int[]{56, 57, 58, 59});
            add(new int[]{60, 61, 62, 63});

            //xy-diagonals
            add(new int[]{0, 5, 10, 15});
            add(new int[]{16, 21, 26, 31});
            add(new int[]{32, 37, 42, 47});
            add(new int[]{48, 53, 58, 63});
            add(new int[]{3, 6, 9, 12});
            add(new int[]{19, 22, 25, 28});
            add(new int[]{35, 38, 41, 44});
            add(new int[]{51, 54, 57, 60});

            //y-lines
            add(new int[]{0, 4, 8, 12});
            add(new int[]{1, 5, 9, 13});
            add(new int[]{2, 6, 10, 14});
            add(new int[]{3, 7, 11, 15});

            add(new int[]{16, 20, 24, 28});
            add(new int[]{17, 21, 25, 29});
            add(new int[]{18, 22, 26, 30});
            add(new int[]{19, 23, 27, 31});

            add(new int[]{32, 36, 40, 44});
            add(new int[]{33, 37, 41, 45});
            add(new int[]{34, 38, 42, 46});
            add(new int[]{35, 39, 43, 47});

            add(new int[]{48, 52, 56, 60});
            add(new int[]{49, 53, 57, 61});
            add(new int[]{50, 54, 58, 62});
            add(new int[]{51, 55, 59, 63});

            //yz-diagonals
            add(new int[]{0, 17, 34, 51});
            add(new int[]{4, 21, 38, 55});
            add(new int[]{8, 25, 42, 59});
            add(new int[]{12, 29, 46, 63});
            add(new int[]{3, 18, 33, 48});
            add(new int[]{7, 22, 37, 52});
            add(new int[]{11, 26, 41, 56});
            add(new int[]{15, 30, 45, 60});

            //z-lines
            add(new int[]{0, 16, 32, 48});
            add(new int[]{1, 17, 33, 49});
            add(new int[]{2, 18, 34, 50});
            add(new int[]{3, 19, 35, 51});

            add(new int[]{4, 20, 36, 52});
            add(new int[]{5, 21, 37, 53});
            add(new int[]{6, 22, 38, 54});
            add(new int[]{7, 23, 39, 55});

            add(new int[]{8, 24, 40, 56});
            add(new int[]{9, 25, 41, 57});
            add(new int[]{10, 26, 42, 58});
            add(new int[]{11, 27, 43, 59});

            add(new int[]{12, 28, 44, 60});
            add(new int[]{13, 29, 45, 61});
            add(new int[]{14, 30, 46, 62});
            add(new int[]{15, 31, 47, 63});

            //xz-diagonals
            add(new int[]{0, 20, 40, 60});
            add(new int[]{1, 21, 41, 61});
            add(new int[]{2, 22, 42, 62});
            add(new int[]{3, 23, 43, 63});
            add(new int[]{12, 24, 36, 48});
            add(new int[]{13, 25, 37, 49});
            add(new int[]{14, 26, 38, 50});
            add(new int[]{15, 27, 39, 51});

            //3d-diagonals
            add(TOPLEFT2BACKRIGHT);
            add(TOPRIGHT2BACKLEFT);
            add(BOTTOMRIGHT2BACKLEFT);
            add(BOTTOMLEFT2BACKRIGHT);
        }};

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lines lines1 = (Lines) o;
        return Objects.equals(lines, lines1.lines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lines);
    }
}
