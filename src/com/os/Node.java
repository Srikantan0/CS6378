package com.os;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Node implements Serializable {
    private final int nodeId;
    private final String hostName;
    private final int port;

    private List<Node> neighbors;
    private NodeState state = NodeState.ACTIVE;
    private int sentMessages = 0;
    private int sentActiveMessages = 0;
    private int maxNumber;
    private VectorClock vectorClock;

    private VectorClock localSnapshot;
    private List<VectorClock> incomingChannelStates = new ArrayList<>();
    private boolean[] markerReceived;
    private boolean isInSnapshot = false;
    private int currentSnapshotId = -1;
    private final int totalNodes;
    private SnapshotProtocol snapshotProtocol;

    Node(int nodeId, String hostName, int port, int totalNodes){
        this.nodeId = nodeId;
        this.hostName = hostName;
        this.port = port;
        this.totalNodes = totalNodes;
        this.neighbors = new ArrayList<>();
        this.vectorClock = new VectorClock(nodeId, totalNodes);
        this.markerReceived = new boolean[0];
    }

    public int getNodeId(){
        return this.nodeId;
    }

    public String getHostName(){
        return this.hostName;
    }

    public int getPort(){
        return this.port;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Node other = (Node) obj;

        return this.nodeId == other.nodeId &&
                this.port == other.port &&
                this.hostName.equals(other.hostName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(nodeId, hostName, port);
    }

    public NodeState getState() {
        return this.state;
    }

    public void setState(NodeState state) {
        this.state = state;
    }

    public int getMaxNumber(){
        return this.maxNumber;
    }

    public int getSentMessages(){
        return this.sentMessages;
    }

    public List<Node> getNeighbors(){
        return this.neighbors;
    }

    public void incrementSentMessages(){
        this.sentMessages += 1;
    }

    public int getSentActiveMessages(){
        return this.sentActiveMessages;
    }

    public void incrementSentActiveMessages(){
        this.sentActiveMessages += 1;
    }

    public void resetSentActiveMessages(){
        this.sentActiveMessages = 0;
    }

    public void setMaxNumber(int maxNumber){
        this.maxNumber = maxNumber;
    }

    public void initClock(int totalNodes) {
        this.vectorClock = new VectorClock(this.nodeId, totalNodes);
    }

    public void incrementVectorClock(){
        this.vectorClock.increment();
    }

    public VectorClock getVectorClock() {
        return this.vectorClock;
    }
    public void setVectorClock(VectorClock vectorClock) {
        this.vectorClock = vectorClock;
    }

    public void updateClock(VectorClock other) {
        if (vectorClock != null) vectorClock.update(other);
    }

    public void initSnapshot(int snapshotId) {
        isInSnapshot = true;
        this.currentSnapshotId = snapshotId;
        incomingChannelStates = new ArrayList<>();
        markerReceived = new boolean[neighbors.size()];
        Arrays.fill(markerReceived, false);
        localSnapshot = vectorClock;
    }

    public void recordIncomingMessage(VectorClock msg, int channel) {
        if (!markerReceived[channel]) {
            incomingChannelStates.add(msg);
        }
    }

    public void markChannelReceived(int channel) {
        markerReceived[channel] = true;
    }

    public boolean isSnapshotComplete() {
        for (boolean b : markerReceived) if (!b) return false;
        return true;
    }

    public VectorClock getLocalSnapshot() {
        return localSnapshot;
    }

    public List<VectorClock> getIncomingChannelStates() {
        return incomingChannelStates;
    }

    public boolean isInSnapshot() {
        return isInSnapshot;
    }
    public void finishSnapshot() {
        isInSnapshot = false;
    }
    public int getTotalNodes(){
        return this.totalNodes;
    }

    public Node getNeighborById(int nodeId) {
        return neighbors.stream()
                .filter(n -> n.getNodeId() == nodeId)
                .findFirst()
                .orElse(null);
    }

    public int getCurrentSnapshotId(){
        return this.currentSnapshotId;
    }
    public void setSnapshotProtocol(SnapshotProtocol sp) {
        this.snapshotProtocol = sp;
    }

    public SnapshotProtocol getSnapshotProtocol() {
        return this.snapshotProtocol;
    }
}