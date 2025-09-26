package com.os;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class parser {
    private int numOfNodes;
    private int minPerActive;
    private int maxPerActive;
    private int minSendDelay;
    private int snapshotDelay;
    private int maxNumberOfMessages;

    private int nodeId;
    private String hostName;
    private  int port;


    public parser() {}

    public void loadFromFile(String path) {
        try (BufferedReader r = new BufferedReader(new FileReader(path))){
            String line;
            int numOfValidLines = 0;
            int numOfLines = 0;

            while((line = r.readLine()) != null) { // readung lines till the parser reads no line
                line = line.trim();

                if (line.isEmpty() || !Character.isDigit(line.charAt(0))){
                    continue;
                }

                String[] inputTokens = line.split("\\s+");

                if(numOfLines == 0) {               // try to read only the first line and print -> this is how i config my system
                    this.numOfNodes = Integer.parseInt(inputTokens[0]);
                    this.minPerActive = Integer.parseInt(inputTokens[1]);
                    this.maxPerActive = Integer.parseInt(inputTokens[2]);
                    this.minSendDelay = Integer.parseInt(inputTokens[3]);
                    this.snapshotDelay = Integer.parseInt(inputTokens[4]);
                }
                else {
                    //todo: need to implement
                }
                numOfLines++;

            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + path);
        } catch (IOException e){
            System.out.println("Error parsing the file: " + e.getMessage());
        }
    }

}