package com.os;

import java.io.*;
import java.net.Socket;

public class TCPClient implements Runnable {
    private static final int MAX_MSG_SIZE = 4096;

    private final Node from;
    private final Node to;
    private final Socket socket;

    public TCPClient(Node from, Node to, Socket socket) {
        this.from = from;
        this.to = to;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            sendMessage(from, to, socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Node from, Node to, Socket socket) throws Exception {
        try {
            socket = new Socket(to.getHostName(), to.getPort());
        } catch (IOException ioe) {
            return;
        }

        from.incrementVectorClock();
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();
        ObjectOutputStream out = new ObjectOutputStream(os);
        ObjectInputStream in = new ObjectInputStream(is);

        String message = "MSG from Node " + from.getNodeId() + " to " + to.getNodeId();
        Message msg = new Message(from.getNodeId(), to.getNodeId(), message);
        msg.messageInfo = from.getVectorClock();

        out.writeObject(msg);
        out.flush();

        Object ack = in.readObject();
        System.out.println("ACK'd': " + ack.toString());

        from.incrementSentMessages();
        from.incrementSentActiveMessages();
    }

    public void sendMarker(Node from, Node to, int snapshotId) throws Exception {
        Socket s = new Socket(to.getHostName(), to.getPort());
        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(s.getInputStream());

        Message markerMsg = new Message(MessageType.MARKER, from.getNodeId());
        markerMsg.snapshotId = snapshotId;
        markerMsg.messageInfo = from.getVectorClock();

        out.writeObject(markerMsg);
        out.flush();

        Object ack = in.readObject();
        System.out.println("ACK for MARKER from Node " + to.getNodeId() + " : " + ack);
    }

    public void sendTerminate() throws Exception {
        try {
            Socket s = new Socket(to.getHostName(), to.getPort());
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            Message terminateMsg = new Message(MessageType.TERMINATE, from.getNodeId());

            out.writeObject(terminateMsg);
            out.flush();
            s.close();
        } catch (IOException e) {
            System.err.println("Error sending TERMINATE signal to Node " + to.getNodeId() + ": " + e.getMessage());
        }
    }

    public void sendSnapshot(Snapshot snapshot) throws Exception {
        try (Socket s = new Socket(to.getHostName(), to.getPort());
             ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream())) {
            Message stateMsg = new Message(MessageType.STATE, from.getNodeId());
            stateMsg.snapshotId = snapshot.snapshotId;
            stateMsg.messageInfo = snapshot;
            out.writeObject(stateMsg);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending snapshot to Node " + to.getNodeId() + ": " + e.getMessage());
        }
    }
}
