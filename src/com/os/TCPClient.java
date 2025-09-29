package com.os;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        } catch (IOException _) {
            return;
        }

        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();

        String message = "Node " +from.getNodeId()+" sending to " + to.getNodeId();
        out.write(message.getBytes());
        out.flush();

        byte[] buf = new byte[MAX_MSG_SIZE];
        int ipBytes = in.read(buf);
        String ack = new String(buf, 0, ipBytes);

        System.out.println("ACK'd': " + ack);

        from.incrementSentMessages();
        from.incrementSentActiveMessages();
    }
}
