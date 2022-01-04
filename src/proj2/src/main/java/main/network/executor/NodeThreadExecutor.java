package main.network.executor;

import main.network.GnuNode;

// Used to create a thread to receive messages of a node
public class NodeThreadExecutor implements NodeExecutor {
    private Thread receiveThread;
    private GnuNode node;
    public NodeThreadExecutor(GnuNode node) {
        this.node = node;
        this.receiveThread = new Thread(node.getMessageHandler());
    }

    @Override
    public void execute() {
        this.receiveThread.start();
    }

    @Override
    public void stop() {
        this.receiveThread.interrupt();
        try {
            this.receiveThread.join();
        } catch (InterruptedException ignored) {}

        this.node.close();
    }
}
