package com.os;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProducerConsumer implements Runnable{

    private Producer producer;
    private Consumer consumer;

    private ExecutorService producerExec;
    private ExecutorService consumerExec;

    ProducerConsumer(
            Producer producer,
            Consumer consumer
    ) {
        this.producer = producer;
        this.consumer = consumer;

        this.producerExec = Executors.newSingleThreadExecutor();
        this.consumerExec = Executors.newSingleThreadExecutor();
    }

    @Override
    public void run() {
        producerExec.submit(producer);
        consumerExec.submit(consumer);
    }

    public void start(){
        Thread pcThread = new Thread(this);
        pcThread.start();
    }

    public void close(){
        producerExec.shutdown();
        consumerExec.shutdown();
    }

}