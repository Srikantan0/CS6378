package com.os;

public class Node {
    private int nodeId;
    private String hostName;
    private  int port;

    Node(int nodeId, String hostName, int port){
        this.nodeId = nodeId;
        this.hostName = hostName;
        this.port = port;
    }
}