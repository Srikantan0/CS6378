package com.os;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import java.io.*;
import java.net.*;

class TCPServerServiceTest {

    private static final int TEST_PORT = 6000;
    private Node testNode;
    private TCPServerService server;
    private Thread serverThread;

    @BeforeEach
    void setUp() throws InterruptedException {
        testNode = new Node(0, "localhost", TEST_PORT);
        testNode.setState(NodeState.PASSIVE);
        testNode.setMaxNumber(1);
        server = new TCPServerService(TEST_PORT, testNode);
        serverThread = new Thread(server);
        serverThread.start();

        Thread.sleep(500);
    }

    @AfterEach
    void tearDown() {
        serverThread.interrupt();
    }

    @Test
    void testServerReceivesMessageAndNodeBecomesActive() throws IOException {
        Socket client = new Socket("localhost", TEST_PORT);
        InputStream in = client.getInputStream();
        OutputStream out = client.getOutputStream();

        String testMessage = "tetsTcpServer";
        out.write(testMessage.getBytes());
        out.flush();

        byte[] buf = new byte[4096];
        int bytesRead = in.read(buf);
        String ack = new String(buf, 0, bytesRead);

        assertEquals("ACK", ack);
        assertEquals(NodeState.ACTIVE, testNode.getState());
    }
}