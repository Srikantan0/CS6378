package com.os;

public class Main {
    public static void main(String[] args) {
        // gonna parse the args to get the node's id
        if(args.length < 1|| args.length > 2){
            System.out.println("Provided either no nodeID, config file path or too many args"); return;
        }
        int currNodeId = Integer.parseInt(args[0]);
        String filePath = args[1];

        Parser parser = new Parser();
        parser.loadFromFile(filePath);
        parser.connectToNeighborasFromCOnfig();

        Node currNode = parser.getNodeById(currNodeId);
        if(currNode == null){
            System.out.println("Input node doesnt match configuration. please check");
            return;
        }
        int minPerActive = parser.getMinPerActive();
        int maxPerActive = parser.getMaxPerActive();
        int minSendDelay = parser.getMinSendDelay();

        Producer producer = new Producer(currNode, minPerActive, maxPerActive,minSendDelay);
        Consumer consumer = new Consumer(currNode);

        ProducerConsumer pc = new ProducerConsumer(producer, consumer);
        pc.start();
        Runtime.getRuntime().addShutdownHook(new Thread(pc::close));
    }
}