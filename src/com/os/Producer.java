package com.os;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Producer implements Runnable{
    Node currentNode;
    int minPerActive;
    Map<Node, Socket> connections;
    int minSendDelay;

    Producer(Node currentNode, int minPerActive, int minSendDelay, Map<Node, Socket> connections){
        this.currentNode = currentNode;
        this.minPerActive = minPerActive;
        this.minSendDelay = minSendDelay;
        this.connections = connections;
    }

    @Override
    public void run() {
        final Random random = new Random();
        if (currentNode.getState() != NodeState.ACTIVE) {
            System.out.println("node "+currentNode.getNodeId()+" passive, cant send. ");
            return;
        }

        int numMessages = random.nextInt(minPerActive + 1);
        List<Node> neighbors = currentNode.getNeighbors();

        for (Node neighbor : neighbors) {
            Socket socket = connections.get(neighbor);
            try {
                if (socket == null || socket.isClosed()) {
                    socket = new Socket(neighbor.getHostName(), neighbor.getPort());
                    connections.put(neighbor, socket);
                }
            } catch (IOException _) {
            }
        }
        List<Node> nodes = new ArrayList<>(connections.keySet());
        for (int i = 0; i < numMessages; i++) {
            if (currentNode.getSentMessages() >= currentNode.getMaxNumber()) {
                System.out.println("node is permanently passive. ");
                break;
            }
            int randIdx = random.nextInt(connections.size());
            Node neighbor = nodes.get(randIdx);
            Socket socket = connections.get(neighbor);
            TCPClientService client = new TCPClientService(currentNode, neighbor, socket);
            Thread clientThread = new Thread(client);
            clientThread.start();

            if (i < numMessages - 1) {
                try {
                    Thread.sleep(minSendDelay);
                } catch (InterruptedException _) { }
            }
        }

        currentNode.setState(NodeState.PASSIVE);
        System.out.println("node"+ currentNode.getNodeId()+" now passive. ");
    }
}
