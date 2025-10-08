package com.os;

import java.util.Map;

public class ChandyLamport implements SnapshotProtocol, Runnable {



    @Override
    public void run() {
        // while(true)
        // periodically take consistent snapshots
        // writeToFile()
        // canTerminate() ?: shutdown() : continue
    }

    @Override
    public void takeSnapshot(int snapshotId) {

    }

    @Override
    public boolean isConsistentGlobalState(Map<Integer, Object> snapshot) {
        return false;
    }

    @Override
    public boolean areAllNodesPassive() {
        return false;
    }

    @Override
    public boolean areAllChannelsEmpty() {
        return false;
    }

    @Override
    public boolean canTerminate() {
        return false;
    }

    @Override
    public void terminate() {

    }
}
