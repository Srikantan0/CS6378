package com.os;

public class Consumer implements Runnable {
    Node currentNode;
    ChandyLamport chandyLamport;

    Consumer(Node node, ChandyLamport chandyLamport) {
        this.currentNode = node;
        this.chandyLamport = chandyLamport;
    }

    @Override
    public void run() {
        TCPServer server = new TCPServer(currentNode, chandyLamport);
        Thread serverThread = new Thread(server);
        serverThread.start();

        System.out.println("node " + currentNode.getNodeId() + "'s server up on " + currentNode.getPort());
    }
}
