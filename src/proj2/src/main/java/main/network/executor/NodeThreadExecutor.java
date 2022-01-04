package main.network.executor;

import main.network.GnuNode;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static main.network.GnuNode.ADDNEIGH_DELAY;
import static main.network.GnuNode.PINGNEIGH_DELAY;

// Used to create a thread to receive messages of a node
public class NodeThreadExecutor implements NodeExecutor {
    private Thread receiveThread;
    private GnuNode node;
    private boolean started;
    private final ScheduledThreadPoolExecutor scheduler;
    private ScheduledFuture<?> pingNeigFuture;
    private ScheduledFuture<?> addNeighFuture;

    public NodeThreadExecutor(GnuNode node, ScheduledThreadPoolExecutor scheduler) {
        this.node = node;
        this.receiveThread = new Thread(node.getMessageHandler());
        this.started = false;
        this.scheduler = scheduler;
    }

    @Override
    public void execute() {
        if (this.started)
            return;

        this.started = true;
        this.receiveThread.start();
        pingNeigFuture = scheduler.scheduleWithFixedDelay(node::pingNeighbours,
                0, PINGNEIGH_DELAY, TimeUnit.MILLISECONDS);
        addNeighFuture = scheduler.scheduleWithFixedDelay(node::addNeighbour,
                0, ADDNEIGH_DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (!this.started)
            return;

        this.started = false;
        this.receiveThread.interrupt();
        try {
            this.receiveThread.join();
        } catch (InterruptedException ignored) {
        }
        pingNeigFuture.cancel(false);
        addNeighFuture.cancel(false);

        this.node.close();
    }
}
