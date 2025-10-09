package com.os;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChandyLamport implements SnapshotProtocol, Runnable {

    private final Node node;
    private final int snapshotDelay;
    private int snapshotId = 0;

    private final Map<Integer, Object> globalSnapshot = new ConcurrentHashMap<>();
    public ChandyLamport(Node node, int snapshotDelay) {
        this.node = node;
        this.snapshotDelay = snapshotDelay;
    }

    @Override
    public void run() {
        if (node.getNodeId() != 0) {
            return;
        }

        try {
            Thread.sleep(5000);
            while (true) {
                Thread.sleep(snapshotDelay);
                if (!node.isInSnapshot()) {
                    System.out.println("Node " + node.getNodeId() + " init a snapshot " + snapshotId);
                    takeSnapshot(snapshotId);
                    snapshotId++;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void takeSnapshot(int currentSnapshotId) {
        node.initSnapshot(currentSnapshotId);
        for (Node neighbor : node.getNeighbors()) {
            if (neighbor.getNodeId() != node.getNodeId()) {
                TCPClient client = new TCPClient(node, neighbor, null);
                new Thread(() -> {
                    try {
                        client.sendMarker(node, neighbor, currentSnapshotId);
                    } catch (Exception e) { }
                }).start();
            }
        }
    }

    @Override
    public boolean isConsistentGlobalState(Map<Integer, Object> snapshot) {
        // TODO: Implement the Vector Clock consistency check here
        return false;
    }

    @Override
    public boolean areAllNodesPassive() {
        // TODO: Implement logic to check if all nodes are Passive in the collected snapshot
        return false;
    }

    @Override
    public boolean areAllChannelsEmpty() {
        // TODO: Implement logic to check if all channels are empty in the collected snapshot
        return false;
    }

    @Override
    public boolean canTerminate() {
        // TODO: Combine the checks for global termination
        return false;
    }

    @Override
    public void terminate() {
        // TODO: Implement the graceful shutdown logic
    }
}