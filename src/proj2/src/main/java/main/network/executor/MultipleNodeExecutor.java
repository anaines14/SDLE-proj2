package main.network.executor;

import main.network.Peer;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class MultipleNodeExecutor {
    public static final int POOL_SIZE = 5;
    private Set<Peer> nodes;
    private boolean started;
    private ScheduledThreadPoolExecutor scheduler;

    public MultipleNodeExecutor(Set<Peer> nodes) {
        this.nodes = nodes;
        this.started = false;
        this.scheduler = new ScheduledThreadPoolExecutor(POOL_SIZE);
    }

    public MultipleNodeExecutor() {
        this(new HashSet<>());
    }

    public boolean addNode(Peer node) {
        if (this.nodes.contains(node))
            return false;

        if (this.started)
            node.execute(scheduler);

        this.nodes.add(node);
        return true;
    }

    public boolean remNode(Peer node) {
        if (!this.nodes.contains(node))
            return false;

        if (this.started)
            node.stop();

        this.nodes.remove(node);
        return true;
    }


    public void execute() {
        if (this.started)
            return;

        this.started = true;
        for (Peer node: nodes)
            node.execute(scheduler);
    }

    public void stop() {
        if (!this.started)
            return;

        this.started = false;
        for (Peer node: nodes)
            node.stop();

        this.scheduler.shutdown();
    }
}
