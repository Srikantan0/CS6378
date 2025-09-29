package com.os;

import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class parserTest {
    private Path tempFile;
    private Parser p = new Parser();

    @Test
    public void testValidSystemConfig() throws IOException {
        Path tempFile = Files.createTempFile("testparser", ".txt");

        String content = """
                5 6 10 100 2000 15
                """;

        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            writer.write(content);
        }

        p.loadFromFile(tempFile.toString());

        int rec_numOfNodes = p.getNumOfNodes();
        int rec_minPerActive = p.getMinPerActive();
        int rec_maxPerActive = p.getMaxPerActive();
        int rec_minSendDelay = p.getMinSendDelay();
        int rec_snapshotDelay = p.getSnapshotDelay();
        int rec_maxNumberOfMessages = p.getMaxNumberOfMessages();

        assertEquals(5, rec_numOfNodes);
        assertEquals(6, rec_minPerActive);
        assertEquals(10, rec_maxPerActive);
        assertEquals(100, rec_minSendDelay);
        assertEquals(2000, rec_snapshotDelay);
        assertEquals(15, rec_maxNumberOfMessages);

        Files.deleteIfExists(tempFile);
    }

    @Test
    public void testInvalidSystemConfigShouldReturn0() throws IOException {
        Path tempFile = Files.createTempFile("testparser", ".txt");

        String content = """
                this config is invalid
                """;

        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            writer.write(content);
        }

        p.loadFromFile(tempFile.toString());

        int rec_numOfNodes = p.getNumOfNodes();
        int rec_minPerActive = p.getMinPerActive();
        int rec_maxPerActive = p.getMaxPerActive();
        int rec_minSendDelay = p.getMinSendDelay();
        int rec_snapshotDelay = p.getSnapshotDelay();
        int rec_maxNumberOfMessages = p.getMaxNumberOfMessages();

        assertEquals(0, rec_numOfNodes);
        assertEquals(0, rec_minPerActive);
        assertEquals(0, rec_maxPerActive);
        assertEquals(0, rec_minSendDelay);
        assertEquals(0, rec_snapshotDelay);
        assertEquals(0, rec_maxNumberOfMessages);

        Files.deleteIfExists(tempFile);
    }

    @Test
    public void testInvalidSystemConfigIgnoreIt() throws IOException {
        Path tempFile = Files.createTempFile("testparser", ".txt");

        String content = """
                this config is invalid
                1 2 3 4 5 6
                """;

        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            writer.write(content);
        }
        p.loadFromFile(tempFile.toString());

        int rec_numOfNodes = p.getNumOfNodes();
        int rec_minPerActive = p.getMinPerActive();
        int rec_maxPerActive = p.getMaxPerActive();
        int rec_minSendDelay = p.getMinSendDelay();
        int rec_snapshotDelay = p.getSnapshotDelay();
        int rec_maxNumberOfMessages = p.getMaxNumberOfMessages();

        assertEquals(1, rec_numOfNodes);
        assertEquals(2, rec_minPerActive);
        assertEquals(3, rec_maxPerActive);
        assertEquals(4, rec_minSendDelay);
        assertEquals(5, rec_snapshotDelay);
        assertEquals(6, rec_maxNumberOfMessages);

        Files.deleteIfExists(tempFile);
    }

    @Test
    public void testSysConfigWithComments() throws IOException {
        Path tempFile = Files.createTempFile("testparser", ".txt");

        String content = """
                this config is invalid
                1 2 3 4 5 6 # this is also ignored
                """;

        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            writer.write(content);
        }

        p.loadFromFile(tempFile.toString());

        int rec_numOfNodes = p.getNumOfNodes();
        int rec_minPerActive = p.getMinPerActive();
        int rec_maxPerActive = p.getMaxPerActive();
        int rec_minSendDelay = p.getMinSendDelay();
        int rec_snapshotDelay = p.getSnapshotDelay();
        int rec_maxNumberOfMessages = p.getMaxNumberOfMessages();

        assertEquals(1, rec_numOfNodes);
        assertEquals(2, rec_minPerActive);
        assertEquals(3, rec_maxPerActive);
        assertEquals(4, rec_minSendDelay);
        assertEquals(5, rec_snapshotDelay);
        assertEquals(6, rec_maxNumberOfMessages);

        Files.deleteIfExists(tempFile);
    }

    @Test
    public void dummyTest() throws IOException {
        Path tempFile = Files.createTempFile("testparser", ".txt");

        String content = """
                this config is invalid
                1 2 3 4 5 6 # this is also ignored
                1 dc01.utd.edu 1111
                """;

        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            writer.write(content);
        }

        p.loadFromFile(tempFile.toString());

        List<Node> rec_nodesInNetwork = p.getAllNodesConfigs();
        List<Node> expected = List.of(
                new Node(1, "dc01.utd.edu", 1111),
                new Node(2, "dc02.utd.edu", 2222)
        );
        assertEquals(expected.getFirst(), rec_nodesInNetwork.getFirst());

        Files.deleteIfExists(tempFile);
    }

    @Test
    public void testWhenConfigHasMoreThanOneNode() throws IOException {
        Path tempFile = Files.createTempFile("testparser", ".txt");

        String content = """
                2 2 3 4 5 6 # this is also ignored
                1 dc01.utd.edu 1111
                2 dc02.utd.edu 2222
                """;

        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            writer.write(content);
        }

        p.loadFromFile(tempFile.toString());

        List<Node> rec_nodesInNetwork = p.getAllNodesConfigs();
        List<Node> expected = List.of(
                new Node(1, "dc01.utd.edu", 1111),
                new Node(2, "dc02.utd.edu", 2222)
        );
        assertEquals(expected.getFirst(), rec_nodesInNetwork.getFirst());
        assertEquals(expected.get(1), rec_nodesInNetwork.get(1));

        Files.deleteIfExists(tempFile);
    }

    @Test
    public void testWhenConfigHasNeighbors() throws IOException {
        Path tempFile = Files.createTempFile("testparser", ".txt");

        String content = """
                2 2 3 4 5 6 # this is also ignored
                1 dc01.utd.edu 1111
                2 dc02.utd.edu 2222
                
                1 2
                2 1
                """;

        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            writer.write(content);
        }

        p.loadFromFile(tempFile.toString());

        Map<Integer, List<Integer>> expected = Map.of(1, List.of(1, 2), 2, List.of(2, 1));
        assertEquals(expected, p.getNeighbors());

        Files.deleteIfExists(tempFile);
    }

}