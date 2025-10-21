package com.os;

import java.util.List;
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

    public void addSnapshot(int nodeId, Snapshot snapshot) {
        if (node.getNodeId() != 0) return;
        if (snapshot.snapshotId == this.snapshotId) {
            System.out.println("Node 0 collected snapshot from Node " + nodeId);
            globalSnapshot.put(nodeId, snapshot);
        }
    }

    @Override
    public void run() {
        if (node.getNodeId() != 0) {
            return;
        }

        try {
            Thread.sleep(5000);
            while (true) {
                if (!globalSnapshot.isEmpty() && globalSnapshot.size() == node.getTotalNodes()) {
                    if (canTerminate()) {
                        terminate();
                        return;
                    }
                    globalSnapshot.clear();
                }
                if (node.getState() == NodeState.PASSIVE && !node.isInSnapshot()) {
                    System.out.println("Node 0 is passive. Initiating GNTD snapshot " + snapshotId);
                    takeSnapshot(snapshotId);
                    snapshotId++;
                }
                Thread.sleep(snapshotDelay);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void takeSnapshot(int currentSnapshotId) {
        if (node.getNodeId() != 0) {
            node.initSnapshot(snapshotId);
            return;
        }
        node.initSnapshot(snapshotId);
        Snapshot localSnapshot = new Snapshot(
                node.getNodeId(),
                node.getCurrentSnapshotId(),
                node.getLocalSnapshot(),
                node.getIncomingChannelStates(),
                node.getState()
        );
        this.addSnapshot(0, localSnapshot);
        System.out.println("Node 0 recorded its own local snapshot in the global map.");
        this.forwardMarkerToConnected(snapshotId);
    }

    private void forwardMarkerToConnected(int currentSnapshotId) {
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
        System.out.println("Checking for global state consistency using Vector Clocks...");
        for (Object receiverObj : snapshot.values()) {
            Snapshot receiverSnapshot = (Snapshot) receiverObj;
            int j = receiverSnapshot.nodeId;
            if (receiverSnapshot.incomingChanelStates == null) continue;
            for (VectorClock messageVC : receiverSnapshot.incomingChanelStates) {
                for (Object senderObj : snapshot.values()) {
                    Snapshot senderSnapshot = (Snapshot) senderObj;
                    int i = senderSnapshot.nodeId;
                    if (i == j) continue;

                    int v_m_i = messageVC.getClock()[i];
                    int v_i_s_i = senderSnapshot.localVectorClock.getClock()[i];
                    int v_m_j = messageVC.getClock()[j];
                    int v_j_s_j = receiverSnapshot.localVectorClock.getClock()[j];
                    if (v_m_i <= v_i_s_i && v_m_j <= v_j_s_j) {
                        System.err.println("Consistency FAILED: Message (VC: " + messageVC + ") collected at Node " + j +
                                " was received *before* j's snapshot and sent *before* i's snapshot. It should not be in the in-transit channel.");
                        return false;
                    }
                }
            }
        }
        System.out.println("Global state is consistent.");
        return true;
    }

    @Override
    public boolean areAllNodesPassive() {
        for (Object obj : globalSnapshot.values()) {
            if (obj instanceof Snapshot) {
                Snapshot snap = (Snapshot) obj;
                if (snap.finalState != NodeState.PASSIVE) {
                    System.out.println("Node " + snap.nodeId + " is not passive (State: " + snap.finalState + ")");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean areAllChannelsEmpty(){
        for (Object obj : globalSnapshot.values()) {
            if (obj instanceof Snapshot) {
                Snapshot snap = (Snapshot) obj;
                if (!snap.incomingChanelStates.isEmpty()) {
                    System.out.println("Node " + snap.nodeId + " recorded " + snap.incomingChanelStates.size() + " in-transit messages.");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean canTerminate() {
        if (!isConsistentGlobalState(globalSnapshot)) {
            System.out.println("Termination check failed: Global state is inconsistent.");
            return false;
        }

        if (!areAllNodesPassive()) {
            System.out.println("Termination check failed: Not all nodes are passive.");
            return false;
        }

        if (!areAllChannelsEmpty()) {
            System.out.println("Termination check failed: Channels are not empty.");
            return false;
        }

        System.out.println("\n*** GLOBAL TERMINATION CONDITION MET ***");
        return true;
    }

    @Override
    public void terminate() {
        node.setHalting(true);
        System.out.println("Node " + node.getNodeId() + " initiating global termination signal to neighbors.");
        for (Node neighbor : node.getNeighbors()) {
            if (neighbor.getNodeId() != node.getNodeId()) {
                TCPClient client = new TCPClient(node, neighbor, null);
                new Thread(() -> {
                    try {
                        client.sendTerminate();
                    } catch (Exception e) {
                    }
                }).start();
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Global termination signal sent. Shutting down Node " + node.getNodeId());
        System.exit(0);
    }
}