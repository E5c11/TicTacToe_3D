package com.esc.test.apps.data.pojos;

public class CubeID {

    private final String coordinates;
    private final String arrayPos;

    public CubeID(String coordinates, String arrayPos) {
        this.coordinates = coordinates;
        this.arrayPos = arrayPos;
    }

    public String getCoordinates() {
        return coordinates;
    }
    public String getArrayPos() {
        return arrayPos;
    }
}
