package dev.cromo29.machines.objects;

public class Fuel {

    private long liters;
    private boolean anyWhere;
    private boolean infinite;

    public Fuel(long liters) {
        this.liters = liters;
    }

    public Fuel(long liters, boolean anyWhere) {
        this.liters = liters;
        this.anyWhere = anyWhere;
    }

    public Fuel (boolean infinite) {
        this.infinite = infinite;
        this.anyWhere = true;
    }

    public long getLiters() {
        return liters;
    }

    public void setLiters(long liters) {
        this.liters = liters;
    }

    public boolean isAnyWhere() {
        return anyWhere;
    }

    public void setAnyWhere(boolean anyWhere) {
        this.anyWhere = anyWhere;
    }

    public boolean isInfinite() {
        return infinite;
    }

    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
    }

    public void decrease() {

        if (infinite) return;

        liters--;

    }
}
