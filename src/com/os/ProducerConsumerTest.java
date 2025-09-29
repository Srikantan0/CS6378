package com.os;

import org.junit.jupiter.api.*;
import java.io.*;
import java.net.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ProducerConsumerMultiNeighborTest {

    private final List<ServerSocket> serverSockets = new ArrayList<>();
    private final List<Thread> serverThreads = new ArrayList<>();

    @AfterEach
    void tearDown() throws Exception {
        for (Thread t : serverThreads) t.interrupt();
        for (ServerSocket s : serverSockets) s.close();
    }

    @Test
    void tetsProducerInNetworkOf3Nodes () throws Exception {
        int numNeighbors = 3;
        List<Node> neighbors = new ArrayList<>();

        for (int i = 0; i < numNeighbors; i++) {
            ServerSocket serverSocket = new ServerSocket(0);
            serverSockets.add(serverSocket);

            Node neighborNode = new Node(
                i + 1,
                "localhost",
                serverSocket.getLocalPort()
            );
            neighborNode.setMaxNumber(5);
            neighbors.add(neighborNode);

            Thread serverThread = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Socket client = serverSocket.accept();
                        InputStream in = client.getInputStream();
                        OutputStream out = client.getOutputStream();

                        byte[] buf = new byte[4096];
                        int read = in.read(buf);
                        if (read > 0) {
                            String msg = new String(buf, 0, read);
                        }

                        out.write("ACK".getBytes());
                        out.flush();
                    }
                } catch (IOException _) {}
            });
            serverThreads.add(serverThread);
            serverThread.start();
        }
        Node currentNode = new Node(0, "localhost", 0);
        currentNode.setMaxNumber(10);
        currentNode.setState(NodeState.ACTIVE);
        currentNode.getNeighbors().addAll(neighbors);

        ProducerConsumer pc = new ProducerConsumer(currentNode, 1, 3, 50);

        pc.produce();
        Thread.sleep(300);

        // Assert
        assertEquals(NodeState.PASSIVE, currentNode.getState());
        assertTrue(currentNode.getSentMessages() > 0);
    }

    @Test
    void testConsumerBringsUpServerInCurrentNodePortToListen() throws Exception{
        Node node = new Node(1, "localhost", 10001);
        node.setMaxNumber(5);
        node.setState(NodeState.PASSIVE);

        ProducerConsumer pc = new ProducerConsumer(node, 1, 3, 100);
        pc.consume();
        Thread.sleep(200);

        Socket clientSocket = new Socket("localhost", node.getPort());
        OutputStream out = clientSocket.getOutputStream();
        InputStream in = clientSocket.getInputStream();

        String testMessage = "testingServer";
        out.write(testMessage.getBytes());

        byte[] buf = new byte[4096];
        int bytesRead = in.read(buf);
        String ack = new String(buf, 0, bytesRead);

        assertEquals("ACK", ack);
        Thread.sleep(200);
        assertEquals(NodeState.ACTIVE, node.getState());
    }
}
