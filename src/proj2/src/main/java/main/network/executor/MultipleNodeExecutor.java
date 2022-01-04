package main.network.executor;

import main.network.Peer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class MultipleNodeExecutor implements NodeExecutor {
    private static final int POOL_SIZE = 5;
    private HashMap<Peer, NodeThreadExecutor> executors;
    private boolean started;
    private ScheduledThreadPoolExecutor scheduler;

    public MultipleNodeExecutor(List<Peer> nodes) {
        this.executors = new HashMap<>();
        this.started = false;
        this.scheduler = new ScheduledThreadPoolExecutor(POOL_SIZE);

        for (Peer node: nodes)
            this.executors.put(node, new NodeThreadExecutor(node, scheduler));
    }

    public MultipleNodeExecutor() {
        this(new ArrayList<>());
    }

    public boolean addNode(Peer node) {
        if (this.executors.containsKey(node))
            return false;

        NodeThreadExecutor executor = new NodeThreadExecutor(node, scheduler);
        if (this.started)
            executor.execute();

        this.executors.put(node, executor);
        return true;
    }

    public boolean remNode(Peer node) {
        if (!this.executors.containsKey(node))
            return false;

        if (this.started)
            this.executors.get(node).stop();

        this.executors.remove(node);
        return true;
    }


    @Override
    public void execute() {
        if (this.started)
            return;

        this.started = true;
        for (Peer node: executors.keySet()) {
            this.executors.get(node).execute();
        }
    }

    @Override
    public void stop() {
        if (!this.started)
            return;

        this.started = false;
        for (Peer node: executors.keySet()) {
            this.executors.get(node).stop();
        }

        this.scheduler.shutdown();
    }
}
