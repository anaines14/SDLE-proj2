package utils;

import main.Peer;
import main.gui.GraphWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class PeerNeighboursFixTest {
    private ScheduledThreadPoolExecutor scheduler;
    private GraphWrapper graph;
    private Peer peer1;
    private Peer peer2;
    private Peer peer3;
    private Peer peer4;

    @BeforeEach
    public void setUp() {
        scheduler = new ScheduledThreadPoolExecutor(10);
        this.graph = new GraphWrapper("Network");

        InetAddress address = null;
        try {
            address = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException ignored) {}

        this.peer1 = new Peer("u1", address, 9);
        this.peer2 = new Peer("u2", address, 6);
        this.peer3 = new Peer("u3", address, 6);
        this.peer4 = new Peer("u4", address, 6);
        loadPeer(peer1);
        loadPeer(peer2);
        loadPeer(peer3);
        loadPeer(peer4);

        this.graph.display();
    }

    public void loadPeer(Peer peer) {
        peer.subscribe(this.graph);
        peer.execute(scheduler);
    }

    @Test
    public void neighbourChangeTest() {
        peer1.join(peer4);
        peer2.join(peer4);
        peer3.join(peer4);

        try {
            Thread.sleep(60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
