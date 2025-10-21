package com.os;

import java.io.Serializable;
import java.util.List;

public class Snapshot implements Serializable {
    private static final long serialVersionUID = 1L;
    int nodeId;
    int snapshotId;
    VectorClock localVectorClock;
    List<VectorClock> incomingChanelStates;
    NodeState finalState;
    int maxMessages; // The project-configured max messages to send
    int messagesSent;

    public Snapshot(
            int nodeId,
            int snapshotId,
            VectorClock vectorClock,
            List<VectorClock> incomingChanelStates,
            NodeState nodeState,
            int maxMessages,
            int messagesSent
    ) {
        this.nodeId = nodeId;
        this.snapshotId = snapshotId;
        this.localVectorClock = vectorClock;
        this.incomingChanelStates = incomingChanelStates;
        this.finalState = nodeState;
        this.maxMessages = maxMessages;
        this.messagesSent = messagesSent;
    }
}
