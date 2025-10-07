package com.os;

import java.io.Serializable;
import java.util.Arrays;

public class VectorClock extends Clock implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int[] clock;  // vector to store timestamps
    public VectorClock() {
        super();
        this.clock = new int[0]; // dummy; will be updated via deserialization
    }

    public VectorClock(int pid, int numProcesses) {
        super(pid);
        this.clock = new int[numProcesses];
        Arrays.fill(this.clock, 0);
    }

    @Override
    public void increment() {
        int idx = pid - 1;
        if (idx < 0 || idx >= clock.length)
            throw new IndexOutOfBoundsException();
        clock[idx]++;
    }

    @Override
    public void update(Clock clock) {
        if (!(clock instanceof VectorClock)) {
            throw new IllegalArgumentException("Expected VectorClock");
        }
        VectorClock otherVectorClock = (VectorClock) clock;

        for (int i = 0; i < this.clock.length; i++) {
            this.clock[i] = Math.max(this.clock[i], otherVectorClock.clock[i]);
        }

        this.clock[pid - 1]++;
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

        if (less && !greater) return -1;
        if (greater && !less) return 1;
        if (!less && !greater) return 0;
        return 2;
    }

    public int[] getClock() {
        return Arrays.copyOf(clock, clock.length);
    }

    @Override
    public String toString() {
        return Arrays.toString(clock);
    }
}
