package com.os;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class ProducerConsumer {
    private final Map<Node, Socket> connections = new HashMap<>();
    private final Node currentNode;
    private final int minPerActive;
    private final int maxPerActive;
    private final int minSendDelay;
    private final Random random = new Random();

    public ProducerConsumer(
            Node currentNode,
            int minPerActive,
            int maxPerActive,
            int minSendDelay
    ) {
        this.currentNode = currentNode;
        this.minPerActive = minPerActive;
        this.maxPerActive = maxPerActive;
        this.minSendDelay = minSendDelay;
    }

    public synchronized void produce() throws Exception {
        if (currentNode.getState() != NodeState.ACTIVE) {
            System.out.println("node passive, cant send. ");
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
            } catch (IOException _) { }
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
                Thread.sleep(minSendDelay);
            }
        }

        currentNode.setState(NodeState.PASSIVE);
        System.out.println("node now passive. ");
    }
}