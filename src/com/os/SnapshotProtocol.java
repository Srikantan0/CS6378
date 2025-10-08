package com.os;

import java.util.Map;

public interface SnapshotProtocol {
    // Initiates local snapshot, sends MARKER messages, waits for markers from all incoming channels
    void takeSnapshot(int snapshotId);

    // Analyze the snapshot and check if it represents a consistent global state
    boolean isConsistentGlobalState(Map<Integer, Object> snapshot);

    // Check if all nodes are currently passive
    boolean areAllNodesPassive();

    // Check if all channels are empty (no in-transit messages)
    boolean areAllChannelsEmpty();

    // Determine whether the MAP protocol can terminate
    boolean canTerminate();

    // Terminate the distributed application gracefully
    void terminate();
}
