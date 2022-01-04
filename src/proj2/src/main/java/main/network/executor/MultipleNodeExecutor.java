package main.network.executor;

import main.network.GnuNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultipleNodeExecutor implements NodeExecutor {
    private HashMap<GnuNode, NodeThreadExecutor> executors;
    private boolean started;

    public MultipleNodeExecutor(List<GnuNode> nodes) {
        this.executors = new HashMap<>();
        this.started = false;

        for (GnuNode node: nodes)
            this.executors.put(node, new NodeThreadExecutor(node));
    }

    public MultipleNodeExecutor() {
        this(new ArrayList<>());
    }

    public boolean addNode(GnuNode node) {
        if (this.executors.containsKey(node))
            return false;

        NodeThreadExecutor executor = new NodeThreadExecutor(node);
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
        this.started = true;
        for (GnuNode node: executors.keySet()) {
            this.executors.get(node).execute();
        }
    }

    @Override
    public void stop() {
        this.started = false;
        for (GnuNode node: executors.keySet()) {
            this.executors.get(node).stop();
        }
    }
}
