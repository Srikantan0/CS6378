package com.os;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class TCPClientTest {

    @Test
    void testTcpClientSnedingToServer() throws Exception {
        ServerSocket serverSocket = new ServerSocket(10001);
        int port = serverSocket.getLocalPort();

        Node from = new Node(1, "localhost", 10000);
        from.setMaxNumber(5);

        Node to = new Node(2, "localhost", port);
        to.setMaxNumber(5);

        new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream();

                byte[] buf = new byte[4096];
                int read = in.read(buf);
                String received = new String(buf, 0, read);
                System.out.println("Server received: " + received);

                out.write("ACK".getBytes());
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        TCPClient client = new TCPClient(from, to, null);
        client.sendMessage(from, to, null);

        assertEquals(1, from.getSentMessages());
        assertEquals(0, to.getSentMessages());

        serverSocket.close();
    }
}