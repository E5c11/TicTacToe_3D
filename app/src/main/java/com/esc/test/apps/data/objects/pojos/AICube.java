package com.esc.test.apps.data.objects.pojos;

public class AICube {

    private int pos;
    private boolean occupied;

    public AICube(int pos, boolean occupied) {
        this.pos = pos;
        this.occupied = occupied;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
}
