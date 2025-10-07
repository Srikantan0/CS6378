package com.os;

import java.util.ArrayList;

public abstract class Clock {

    protected int pid; // process ID of the node

    public Clock(int pid) {
        this.pid = pid;
    }

    public abstract void increment();

    public abstract void update(Clock other);

    public abstract int compare(Clock other);
}