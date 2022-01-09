import main.Peer;
import main.gui.GraphWrapper;
import main.model.neighbour.Neighbour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BloomFiltersTest {
    private GraphWrapper graph;
    private InetAddress address;
    private ScheduledThreadPoolExecutor scheduler;
    private Peer peer1, peer2, peer3;

    private static int MIN_NODE_SIZE = 2;
    private static  int MAX_NODE_SIZE = 10;

    @BeforeEach
    public void setUp() {
        scheduler = new ScheduledThreadPoolExecutor(10);
        this.graph = new GraphWrapper("Network");

        address = null;
        try {
            address = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}

        this.graph.display();

        peer1 = new Peer("peer1", address, 30);
        peer2 = new Peer("peer2", address, 50);
        peer3 = new Peer("peer3", address, 3);

        peer3.join(peer2);
        peer1.join(peer2);

        peer1.subscribe(this.graph);
        peer2.subscribe(this.graph);
        peer3.subscribe(this.graph);

    }

    @Test
    public void bloomFilterDirectNeighbours() {
        peer1.execute(scheduler);
        peer2.execute(scheduler);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Set<Neighbour> peers = peer1.getPeerInfo().getNeighboursWithTimeline("peer2");
        assertEquals(1, peers.size());
        assertTrue(peers.contains(new Neighbour(peer2.getPeerInfo().getHost())));
    }

    @Test
    public void bloomFilterMiddleSuperPeer() {
        peer1.execute(scheduler);
        peer2.execute(scheduler);
        peer3.execute(scheduler);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Set<Neighbour>peers = peer1.getPeerInfo().getNeighboursWithTimeline("peer3");
        assertEquals(1, peers.size());
        assertTrue(peers.contains(new Neighbour(peer2.getPeerInfo().getHost())));
    }
}
