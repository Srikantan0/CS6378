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
                if (!globalSnapshot.isEmpty()) {
                    if (canTerminate()) {
                        System.out.println("Terminating--");
                        terminate();
                        return;
                    }
                    globalSnapshot.clear();
                }
                if (node.getState() == NodeState.PASSIVE && !node.isInSnapshot()) {
                    System.out.println("Node 0 is passive => snapshot " + snapshotId);
                    takeSnapshot(++snapshotId);

                }
                Thread.sleep(snapshotDelay);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void takeSnapshot(int currentSnapshotId) {
        node.initSnapshot(snapshotId);
        Snapshot localSnapshot = new Snapshot(
                node.getNodeId(),
                node.getCurrentSnapshotId(),
                node.getLocalSnapshot(),
                node.getIncomingChannelStates(),
                node.getState(),
                node.getMaxNumber(),
                node.getSentActiveMessages()
        );
        this.addSnapshot(node.getNodeId(), localSnapshot);
        System.out.println("Node "+node.getNodeId()+" recorded its own local snapshot in the global map.");
        this.forwardTerminationToAll(snapshotId);
    }

    private void forwardTerminationToAll(int currentSnapshotId) {
        for (Node neighbor : node.getNeighbors()) {
            System.out.println("sending terminate signal to all");
            if (neighbor.getNodeId() != node.getNodeId()) {
                TCPClient client = new TCPClient(node, neighbor, null);
                new Thread(() -> {
                    try {
                        client.sendTerminate();
                    } catch (Exception e) { }
                }).start();
            }
            System.out.println("done with termination signals");
        }
    }

    @Override
    public boolean isConsistentGlobalState(Map<Integer, Object> snapshot) {
        System.out.println("consistency check for clocks");
        for (Object receiverObj : snapshot.values()) {
            Snapshot receiverSnapshot = (Snapshot) receiverObj;
            int j = receiverSnapshot.nodeId;
            if (receiverSnapshot.incomingChanelStates == null) continue;
            for (VectorClock messageVC : receiverSnapshot.incomingChanelStates) {
                int i = messageVC.pid;
                Snapshot senderSnapshot = (Snapshot) snapshot.get(i);
                if (senderSnapshot == null) continue;

                int v_m_i = messageVC.getClock()[i];
                int v_i_s_i = senderSnapshot.localVectorClock.getClock()[i];

                if (v_m_i <= v_i_s_i) {
                    System.out.println("inconsistent clocks found: Message from Node " + i + " to Node " + j +
                            " violates consistency (C(m)_i=" + v_m_i + " <= V_i(S_i)_i=" + v_i_s_i + ")");
                    System.out.println("inconsistent");
                    return false;
                }
            }
        }
        System.out.println("consistent");
        return true;
    }

    @Override
    public boolean areAllNodesPassive() {
        for (Object obj : globalSnapshot.values()) {
            if (obj instanceof Snapshot) {
                Snapshot snap = (Snapshot) obj;
                if (snap.finalState != NodeState.PASSIVE) {
                    System.out.println("node " + snap.nodeId + " " + snap.finalState);
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
                    System.out.println("recorded intransit message");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean canTerminate() {
        if (!isConsistentGlobalState(globalSnapshot)) {
            System.out.println("not consistent");
            return false;
        }

        if (!areAllNodesPassive()) {
            System.out.println("some nodes are still active");
            return false;
        }

        if (!areAllChannelsEmpty()) {
            System.out.println("intransit messages lost");
            return false;
        }

        System.out.println("terminating...");
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
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("gracefully shutting down node");
        VectorClock locSnap = node.getLocalSnapshot();
        if(locSnap == null) locSnap = node.getVectorClock();
        node.addCompletedSnapshot(locSnap);
        node.shutdownGracefully();
        System.exit(0);
    }
}