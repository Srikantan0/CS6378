package com.os;

import java.io.Serializable;
import java.util.Arrays;

public class VectorClock extends Clock implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int[] clock;  // vector to store timestamps

    public VectorClock(int pid, int numProcesses) {
        super(pid);
        this.clock = new int[numProcesses];
        Arrays.fill(this.clock, 0);
    }

    @Override
    public void increment() {
        if (pid < 0 || pid >= clock.length)
            throw new IndexOutOfBoundsException();
        synchronized (clock){
            clock[pid]++;
        }
    }

    @Override
    public void update(Clock clock) { // chcek
        if (!(clock instanceof VectorClock)) {
            throw new IllegalArgumentException("Expected VectorClock");
        }
        VectorClock otherVectorClock = (VectorClock) clock;

        for (int i = 0; i < this.clock.length; i++) {
            synchronized (this.clock){
                this.clock[i] = Math.max(this.clock[i], otherVectorClock.clock[i]);
            }
        }
    }

    @Override
    public int compare(Clock clock) {
        if (!(clock instanceof VectorClock)) {
            throw new IllegalArgumentException("Expected VectorClock");
        }
        VectorClock otherVectorClock = (VectorClock) clock;

        boolean less = false;
        boolean greater = false;

        for (int i = 0; i < this.clock.length; i++) {
            if (this.clock[i] < otherVectorClock.clock[i]) less = true;
            if (this.clock[i] > otherVectorClock.clock[i]) greater = true;
        }

        if (less && !greater) return -1; // this is lesser
        if (greater && !less) return 1; // other clock is lesser
        if (!less && !greater) return 0; // concurrent
        return 0;
    }

    public int[] getClock() {
        return Arrays.copyOf(clock, clock.length);
    }

    @Override
    public String toString() {
        return Arrays.toString(clock);
    }
}
