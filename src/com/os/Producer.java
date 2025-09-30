package com.os;

import java.util.*;
public class Producer implements Runnable{
    Node currentNode;
    int minPerActive;
    int minSendDelay;

    Producer(Node currentNode, int minPerActive, int minSendDelay){
        this.currentNode = currentNode;
        this.minPerActive = minPerActive;
        this.minSendDelay = minSendDelay;
    }

    @Override
    public void run() {
        final Random random = new Random();
        if (currentNode.getState() != NodeState.ACTIVE) {
            System.out.println("node "+currentNode.getNodeId()+" passive, cant send. ");
            return;
        }

        int numMessages = minPerActive + 1;
        List<Node> neighbors = currentNode.getNeighbors();

        for (int i = 0; i < numMessages; i++) {
            if (currentNode.getSentMessages() >= currentNode.getMaxNumber()) {
                System.out.println("node " + currentNode.getNodeId() + " is permanently passive.");
                break;
            }

            Node neighbor = neighbors.get(random.nextInt(neighbors.size()));
            TCPClient client = new TCPClient(currentNode, neighbor, null);
            Thread clientThread = new Thread(client);
            clientThread.start();

            if (i < numMessages - 1) {
                try {
                    Thread.sleep(minSendDelay);
                } catch (InterruptedException ie) { }
            }
        }

        currentNode.setState(NodeState.PASSIVE);
        currentNode.resetSentActiveMessages();
        System.out.println("Node " + currentNode.getNodeId() + " now passive.");
    }
}
