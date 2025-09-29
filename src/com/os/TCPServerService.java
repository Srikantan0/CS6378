package com.os;

import com.sun.nio.sctp.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

// listen on port of curr node for any incoming msg from any neighbor node
// ack that you've recd a message
public class TCPServerService implements Runnable {
    private static final int MAX_MSG_SIZE = 4096;

    private int port;
    private Node node;

    public TCPServerService(int port, Node node) {
        this.port = port;
        this.node = node;
    }

    @Override
    public void run() {
        try {
            startServer(port, node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startServer(int port, Node node) throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("TCP Server listening -> port " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            byte[] buf = new byte[MAX_MSG_SIZE];
            int bytesRead = in.read(buf);

            String received = new String(buf, 0, bytesRead);
            System.out.println("Node " + node.getNodeId() + " received: " + received);

            out.write("ACK".getBytes());
            out.flush();

            if (node.getState() == NodeState.PASSIVE && node.getSentMessages() < node.getMaxNumber()) {
                node.setState(NodeState.ACTIVE);
                System.out.println("Node " + node.getNodeId() + " became ACTIVE");
            }

        }
    }
}