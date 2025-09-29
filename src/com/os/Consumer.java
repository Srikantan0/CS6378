package com.os;

public class Consumer implements Runnable {
    Node currentNode;

    Consumer(Node node) {
        this.currentNode = node;
    }

    @Override
    public void run() {
        TCPServerService server = new TCPServerService(currentNode);
        Thread serverThread = new Thread(server);
        serverThread.start();

        System.out.println("node " + currentNode.getNodeId() + "'s server up on " + currentNode.getPort());
    }
}
