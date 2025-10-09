package com.os;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChandyLamport implements SnapshotProtocol, Runnable {

    private final Node node;
    private final int snapshotDelay;
    private int snapshotId = 0;
    private final List<Node> nodesInNetwork;

    private final Map<Integer, Object> globalSnapshot = new ConcurrentHashMap<>();
    public ChandyLamport(Node node, int snapshotDelay, List<Node> nodes) {
        this.node = node;
        this.snapshotDelay = snapshotDelay;
        this.nodesInNetwork = nodes;
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
                TCPClient client = new TCPClient(node, neighbor);
                new Thread(() -> {
                    try {
                        client.sendMarker(node, neighbor, currentSnapshotId);
                    } catch (Exception e) { }
                }).start();
            }
        }
    }

    public synchronized void updateGlobalSnapshot(Snapshot snapshot) {
        globalSnapshot.put(snapshot.nodeId, snapshot);
        if (globalSnapshot.size() == nodesInNetwork.size()) {
            System.out.println("global snapshot: " + snapshot.snapshotId + " over");

            if (isConsistentGlobalState(globalSnapshot)) {
                System.out.println("snapsshot " + snapshot.snapshotId + " consistent");
            } else {
                System.out.println("snapshot " + snapshot.snapshotId + " inconsistent");
            }
            globalSnapshot.clear();
        }
    }

    @Override
    public boolean isConsistentGlobalState(Map<Integer, Object> snapshot) {
        for (Object dataObj : snapshot.values()) {
            Snapshot recdData = (Snapshot) dataObj;
            VectorClock vc = recdData.localVectorClock;
            for (VectorClock sentVc : recdData.incomingChanelStates) {
                int senderId = sentVc.pid;
                int sendVC = sentVc.getClock()[senderId];
                int recdC = vc.getClock()[senderId];
                if (sendVC <= recdC) return false;
            }
        }
        return true;
    }

    public Node getInitatorNodeId() {
        return nodesInNetwork.stream()
                .filter(n -> n.getNodeId() == 0)
                .findFirst().orElse(null);
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
    public void terminate() { }
}