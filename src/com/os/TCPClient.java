package com.os;

import java.io.*;
import java.net.Socket;

public class TCPClient implements Runnable {

    private final Node from;
    private final Node to;

    public TCPClient(Node from, Node to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void run() {
        try {
            sendMessage(from, to);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Node from, Node to) throws Exception {
        Socket socket = null;
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

    public void sendState(Node from, Node to, Snapshot localSnapshot) throws Exception {
        Socket s = null;
        try {
            s = new Socket(to.getHostName(), to.getPort());
        } catch (IOException ioe) { }

        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(s.getInputStream());

        Message stateMsg = constrcutLocalStateMessage(from, localSnapshot);
        System.out.println("Node " + from.getNodeId() + "snapshot DONE -> reporting to 0");
        out.writeObject(stateMsg);
        out.flush();

        Object ack = in.readObject();
        System.out.println("SNAPSHOT REPORT ACK from " + to.getNodeId() + " : " + ack);
        in.close();
        out.close();
        s.close();
    }

    private static Message constrcutLocalStateMessage(Node from, Snapshot localSnapshot) {
        Message stateMsg = new Message(MessageType.STATE, from.getNodeId());
        stateMsg.snapshotId = localSnapshot.snapshotId;
        stateMsg.messageInfo = localSnapshot;
        return stateMsg;
    }

}
