package com.os;

import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class parserTest {
    private Path tempFile;
    private parser p = new parser();

    @Test
    public void testValidSystemConfig() throws IOException {
        Path tempFile = Files.createTempFile("testparser", ".txt");

        String content = """
            5 6 10 100 2000 15
            """;

        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            writer.write(content);
        }

        parser p = new parser();
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
        assertEquals( 2000, rec_snapshotDelay);
        assertEquals(15, rec_maxNumberOfMessages);

        Files.deleteIfExists(tempFile);
    }
}