package com.os;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ProducerConsumerIntegrationTest {

    @Test
    public void testServerAndClientsWithProducerConsumer() throws Exception {
        Node serverNode = new Node(100, "localhost", 10001);
        serverNode.setMaxNumber(10);
        serverNode.setState(NodeState.PASSIVE);

        ProducerConsumer serverPC = new ProducerConsumer(serverNode, 1, 3, 50);
        serverPC.consume();
        Thread.sleep(300);

        Node client1 = new Node(1, "localhost", 10002);
        client1.setMaxNumber(5);
        client1.setState(NodeState.ACTIVE);

        Node client2 = new Node(2, "localhost", 10003);
        client2.setMaxNumber(5);
        client2.setState(NodeState.ACTIVE);

        Node client3 = new Node(3, "localhost", 10004);
        client3.setMaxNumber(5);
        client3.setState(NodeState.ACTIVE);

        client1.getNeighbors().addAll(Arrays.asList(serverNode));
        client2.getNeighbors().addAll(Arrays.asList(serverNode));
        client3.getNeighbors().addAll(Arrays.asList(serverNode));

        ProducerConsumer pc1 = new ProducerConsumer(client1, 1, 2, 50);
        ProducerConsumer pc2 = new ProducerConsumer(client2, 1, 2, 50);
        ProducerConsumer pc3 = new ProducerConsumer(client3, 1, 2, 50);

        Thread t1 = new Thread(() -> {
            try {
                pc1.produce();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                pc2.produce();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread t3 = new Thread(() -> {
            try {
                pc3.produce();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        Thread.sleep(300);
        assertEquals(NodeState.ACTIVE, serverNode.getState());
    }

    @Test @Disabled
    public void testFullyConnectedMesh() throws Exception {
        int numNodes = 4;
        int basePort = 8200;
        List<Node> nodes = new ArrayList<>();
        List<ProducerConsumer> pcs = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            Node node = new Node(i, "localhost", basePort + i);
            node.setMaxNumber(10);
            node.setState(i == 0 ? NodeState.ACTIVE : NodeState.PASSIVE);
            nodes.add(node);
        }

        for (Node node : nodes) {
            List<Node> neighbors = new ArrayList<>(nodes);
            neighbors.remove(node);
            node.getNeighbors().addAll(neighbors);
        }

        for (Node node : nodes) {
            ProducerConsumer pc = new ProducerConsumer(node, 1, 3, 50);
            pcs.add(pc);
            pc.consume();
        }

        Thread.sleep(500);

        List<Thread> clients = new ArrayList<>();
        for (ProducerConsumer pc : pcs) {
            Thread t = new Thread(() -> {
                try {
                    pc.produce();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            clients.add(t);
            t.start();
        }

        for (Thread t : clients) {
            t.join();
        }

        boolean anyNodeActive = nodes.stream().anyMatch(n -> n.getState() == NodeState.ACTIVE);

        assertTrue(nodes.stream().anyMatch(n -> n.getSentMessages() > 0));
    }
}
