package com.os;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
                    System.out.println("MARKER REC'd :: " + node.getNodeId());
                    // TODO: Add snapshot handling here
                }
                out.writeObject("ACK'd your VC : " + msg.messageInfo);
                out.flush();
            if (node.getState() == NodeState.PASSIVE && node.getSentMessages() < node.getMaxNumber()) {
                    node.setState(NodeState.ACTIVE);
                    System.out.println("Node " + node.getNodeId() + " became ACTIVE");
                }
            }

        }
    }
}

// is it appln or marker message ?: proceed ; logic
// marker -> clock will be available -> compare message' clock & node.clock -> update node's clock with other clock if needed
///     public void startServer(Node node) throws Exception {
///         ServerSocket serverSocket = new ServerSocket(node.getPort());
///         System.out.println("node "+ node.getNodeId()+"'s tcp server up on" + node.getPort());
///
///         while (true) {
///             Socket socket = serverSocket.accept();
///             InputStream in = socket.getInputStream();
///             OutputStream out = socket.getOutputStream();
///
///             byte[] buf = new byte[MAX_MSG_SIZE];
///             int bytesRead = in.read(buf);
///
///             String received = new String(buf, 0, bytesRead);
///             System.out.println("Msg recd from" + node.getNodeId() + " : " + received);
///
///             out.write("ACK".getBytes());
///             out.flush();
///
///             if (node.getState() == NodeState.PASSIVE && node.getSentMessages() < node.getMaxNumber()) {
///                 node.setState(NodeState.ACTIVE);
///                 System.out.println("Node " + node.getNodeId() + " became ACTIVE");
///             }
///
///         }
///     }
/// }