package com.os;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProducerConsumer implements Runnable{

    private final Producer producer;
    private final Consumer consumer;

    private final ExecutorService producerExec;
    private final ExecutorService consumerExec;

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
        producerExec.execute(producer);
        consumerExec.execute(consumer);
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