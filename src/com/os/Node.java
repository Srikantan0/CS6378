package com.os;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private final int nodeId;
    private final String hostName;
    private final int port;

    private List<Node> neighbors;
    private NodeState state;
    private int sentMessages = 0;
    private int sentActiveMessages = 0;
    private int maxNumber;

    Node(int nodeId, String hostName, int port){
        this.nodeId = nodeId;
        this.hostName = hostName;
        this.port = port;
        this.neighbors = new ArrayList<>();
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

}