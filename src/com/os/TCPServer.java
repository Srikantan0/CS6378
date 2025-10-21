package com.os;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

// listen on port of curr node for any incoming msg from any neighbor node
// ack that you've recd a message
public class TCPServer implements Runnable {
    private static final int MAX_MSG_SIZE = 4096;

    private final Node node;

    public TCPServer(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        try {
            startServer(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startServer(Node node) throws Exception {
        ServerSocket serverSocket = new ServerSocket(node.getPort());
        System.out.println("node " + node.getNodeId() + "'s tcp server up on " + node.getPort());

        while (true) {
            Socket socket = serverSocket.accept();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Object obj = in.readObject();
            System.out.println("hello world");

            if (obj instanceof Message) {
                Message msg = (Message) obj;
                System.out.println(
                        "Msg recd by Node " + node.getNodeId() +
                                " from Node " + msg.fromNodeId +
                                " : " + msg.messageInfo
                );
                if (msg.msgType == MessageType.STATE) {
                    System.out.println("Node " + node.getNodeId() + " received state messages from Node " + msg.fromNodeId);
                    if (node.getNodeId() == 0 && msg.messageInfo instanceof Snapshot) {
                        ChandyLamport cl = (ChandyLamport) node.getSnapshotProtocol();
                        cl.addSnapshot(msg.fromNodeId, (Snapshot) msg.messageInfo);
                    }
                    return;
                }

                if (msg.msgType == MessageType.TERMINATE) {
                    System.out.println("Node " + node.getNodeId() + " received TERMINATE signal. Shutting down.");
                    ChandyLamport cl = (ChandyLamport) node.getSnapshotProtocol();
                    cl.terminate();
                    return;
                }
                if (msg.msgType == MessageType.MARKER) {
                    System.out.println("MARKER received by Node " + node.getNodeId() + " from Node " + msg.fromNodeId);
                    Node sender = node.getNeighbors().stream()
                            .filter(n -> n.getNodeId() == msg.fromNodeId)
                            .findFirst()
                            .orElse(null);
                    if (sender == null) return;

                    int channelIdx = node.getNeighbors().indexOf(sender);

                    if (node.getIncomingChannelStates() == null || node.getIncomingChannelStates().isEmpty()) {
                        System.out.println("Node " + node.getNodeId() + " initialized snapshot on first marker");
                    }

                    if (!node.isInSnapshot()) {
                        node.initSnapshot(msg.snapshotId);
                        System.out.println("Node " + node.getNodeId() + " init a snapshot");
                        for (Node neighbor : node.getNeighbors()) {
                            if (neighbor.getNodeId() != sender.getNodeId()) {
                                TCPClient client = new TCPClient(node, neighbor, null);
                                new Thread(() -> {
                                    try {
                                        client.sendMarker(node, neighbor, msg.snapshotId);
                                    } catch (Exception e) {
                                    }
                                }).start();
                            }
                        }
                    } else {
                        System.out.println("Recorded in-transit message on channel " + msg.fromNodeId + "->" + node.getNodeId());
                        node.recordIncomingMessage((VectorClock) msg.messageInfo, channelIdx);
                    }
                    node.markChannelReceived(channelIdx);
                    if (node.isSnapshotComplete()) {
                        System.out.println("Node " + node.getNodeId() + " snapshot completed. Sending to Node 0.");

                        Snapshot localSnapshot = new Snapshot(
                                node.getNodeId(),
                                node.getCurrentSnapshotId(),
                                node.getLocalSnapshot(),
                                node.getIncomingChannelStates(),
                                node.getState(),
                                node.getMaxNumber(),
                                node.getSentActiveMessages()
                        );

                        Node initiator = node.getNeighborById(0);
                        if (initiator != null) {
                            TCPClient client = new TCPClient(node, initiator, null);
                            new Thread(() -> {
                                try {
                                    client.sendSnapshot(localSnapshot);
                                } catch (Exception e) {
                                    System.err.println("Error sending snapshot: " + e.getMessage());
                                }
                            }).start();
                        }
                        node.finishSnapshot();
                    }
                }
                out.writeObject("ACK'd your VC : " + msg.messageInfo);
                out.flush();
                if (node.getState() == NodeState.PASSIVE && node.getSentMessages() < node.getMaxNumber()) {
                    node.setState(NodeState.ACTIVE);
                    System.out.println("Node " + node.getNodeId() + " became ACTIVE");
                }
                if (msg.msgType == MessageType.APP) {
                    node.updateClock((VectorClock) msg.messageInfo);
                }
            }

        }
    }
}