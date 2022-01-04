package main.network.executor;

import main.network.GnuNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class MultipleNodeExecutor implements NodeExecutor {
    private static final int POOL_SIZE = 5;
    private HashMap<GnuNode, NodeThreadExecutor> executors;
    private boolean started;
    private ScheduledThreadPoolExecutor scheduler;

    public MultipleNodeExecutor(List<GnuNode> nodes) {
        this.executors = new HashMap<>();
        this.started = false;
        this.scheduler = new ScheduledThreadPoolExecutor(POOL_SIZE);

        for (GnuNode node: nodes)
            this.executors.put(node, new NodeThreadExecutor(node, scheduler));
    }

    public MultipleNodeExecutor() {
        this(new ArrayList<>());
    }

    public boolean addNode(GnuNode node) {
        if (this.executors.containsKey(node))
            return false;

        NodeThreadExecutor executor = new NodeThreadExecutor(node, scheduler);
        if (this.started)
            executor.execute();

        this.executors.put(node, executor);
        return true;
    }

    public boolean remNode(GnuNode node) {
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
        for (GnuNode node: executors.keySet()) {
            this.executors.get(node).execute();
        }
    }

    @Override
    public void stop() {
        if (!this.started)
            return;

        this.started = false;
        for (GnuNode node: executors.keySet()) {
            this.executors.get(node).stop();
        }

        this.scheduler.shutdown();
    }
}
