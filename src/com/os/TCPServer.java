package com.os;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

// listen on port of curr node for any incoming msg from any neighbor node
// ack that you've recd a message
public class TCPServer implements Runnable {

    private final Node node;
    private final ChandyLamport chandyLamport;

    public TCPServer(Node node, ChandyLamport chandyLamport) {
        this.node = node;
        this.chandyLamport = chandyLamport;
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
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            Object obj = in.readObject();

            if (obj instanceof Message) {
                Message msg = (Message) obj;
                System.out.println(
                        "Msg recd by Node " + node.getNodeId() +
                                " from Node " + msg.fromNodeId +
                                " : " + msg.messageInfo
                );
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
                        initiateMarkerMessages(node, msg, sender);
                    } else {
                        System.out.println("rec'd channel state: " + msg.fromNodeId + "->" + node.getNodeId());
                        VectorClock tempClock = (VectorClock) msg.messageInfo;
                        node.recordIncomingMessage(tempClock, channelIdx);
                    }
                    node.markChannelReceived(channelIdx);
                    if (node.isSnapshotComplete()) {
                        System.out.println("Node " + node.getNodeId() + " snapshot completed");
                        Snapshot snapshot = new Snapshot(
                                node.getNodeId(),
                                node.getCurrentSnapshotId(),
                                node.getLocalSnapshot(),
                                node.getIncomingChannelStates(),
                                node.getState()
                        );
                        if (node.getNodeId() != 0) {
                            sendMarkerToInitiator(node, snapshot);
                        } else {
                            chandyLamport.updateGlobalSnapshot(snapshot);
                        }
                        System.out.println("Local state: " + snapshot.localVectorClock);
                        System.out.println("Channel states " + snapshot.incomingChanelStates);
                        node.finishSnapshot();
                    }
                }
                else if (msg.msgType == MessageType.STATE) {
                    handleStateMessages(node, msg);
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
                if (msg.msgType == MessageType.APP && node.isInSnapshot()) {
                    recordingChannelStates(node, msg);
                }
            }

        }
    }

    private static void initiateMarkerMessages(Node node, Message msg, Node sender) {
        node.initSnapshot(msg.snapshotId);
        System.out.println("Node " + node.getNodeId() + " init a snapshot");
        for (Node neighbor : node.getNeighbors()) {
            if (neighbor.getNodeId() != sender.getNodeId()) {
                TCPClient client = new TCPClient(node, neighbor);
                new Thread(() -> {
                    try {
                        client.sendMarker(node, neighbor, msg.snapshotId);
                    } catch (Exception e) {
                    }
                }).start();
            }
        }
    }

    private static void recordingChannelStates(Node node, Message msg) {
        int channelIdx = -1;
        List<Node> neighbors = node.getNeighbors();
        for (int i = 0; i < neighbors.size(); i++) {
            if (neighbors.get(i).getNodeId() == msg.fromNodeId) {
                channelIdx = i;
                break;
            }
        }
        if (channelIdx != -1) {
            node.recordIncomingMessage((VectorClock) msg.messageInfo, channelIdx);
            System.out.println("channelState :" + msg.fromNodeId + "->" + node.getNodeId());
        } else {
            System.out.println("WRONG CHANNEL MESSAGE, ignorning");
        }
    }

    private void handleStateMessages(Node node, Message msg) {
        if (node.getNodeId() == 0) {
            Snapshot snapshot = (Snapshot) msg.messageInfo;
            System.out.println("Initiator node got the snapshot from " + snapshot.nodeId);
            chandyLamport.updateGlobalSnapshot(snapshot);
        } else {
            System.out.println("no init node got a global snapshot");
        }
    }

    private void sendMarkerToInitiator(Node node, Snapshot snapshot) {
        Node initiatorNode = chandyLamport.getInitatorNodeId();
        if (initiatorNode != null) {
            new Thread(() -> {
                try {
                    TCPClient client = new TCPClient(node, initiatorNode);
                    client.sendState(node, initiatorNode, snapshot);
                } catch (Exception e) { }
            }).start();
        }
    }
}