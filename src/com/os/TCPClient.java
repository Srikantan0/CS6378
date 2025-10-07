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
}

/// public void sendMessage(Node from, Node to, Socket socket) throws Exception {
///         try {
///             socket = new Socket(to.getHostName(), to.getPort());
///         } catch (IOException ioe) {
///             return;
///         }
///
///         from.incrementVectorClock();
///         OutputStream os = socket.getOutputStream();
///         InputStream is = socket.getInputStream();
///         ObjectOutputStream out = new ObjectOutputStream(os);
///         ObjectInputStream in = new ObjectInputStream(is);
///
///         String message = "MSG from Node " +from.getNodeId()+" to " + to.getNodeId() + "| "+ from.getVectorClock();
///         Message msg = new Message(from.getNodeId(), to.getNodeId(), message);
///         msg.messageInfo = from.getVectorClock();
///         out.writeObject(msg);
///         out.flush();
///
///         Object ack = in.readObject();
///
///         System.out.println("ACK'd': " + ack.toString());
///
///         from.incrementSentMessages();
///         from.incrementSentActiveMessages();
///     }
