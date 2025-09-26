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
            5 1 3 100 2000 10
            """;

        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            writer.write(content);
        }

        parser cfg = new parser();
        cfg.loadFromFile(tempFile.toString());

        System.out.println(p);

        Files.deleteIfExists(tempFile);
    }
}