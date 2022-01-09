import main.Peer;
import main.controller.message.MessageSender;
import main.gui.GraphWrapper;
import main.model.neighbour.Neighbour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BloomFiltersTest {
    private GraphWrapper graph;
    private InetAddress address;
    private ScheduledThreadPoolExecutor scheduler;

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
    }

    public List<Peer> nodeFactory() {
        String username = "user";
        Random rand = new Random();
        List<Peer> peers = new ArrayList<>();

        // initiator peer
        Peer initPeer = new Peer(username + 1, address, 30);
        initPeer.subscribe(this.graph);
        initPeer.execute(scheduler);
        peers.add(initPeer);

        // super peers
        for (int i = 2; i <= 3; i++) {
            Peer peer = new Peer(username + i, address, 30);
            peer.subscribe(this.graph);
            peer.execute(scheduler);
            peers.add(peer);
            peer.join(initPeer);
        }

        for(int i = 4; i <= 12; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int capacity = MIN_NODE_SIZE + rand.nextInt(MAX_NODE_SIZE);
            Peer p = new Peer(username + i, address, capacity);
            p.subscribe(this.graph);
            p.execute(scheduler);
            p.join(peers.get((i-3) % 3));
            System.out.println((i-3) % 3);
            peers.add(p);
        }

        return peers;
    }

    @Test
    public void bloomFilter() {
        Peer peer1 = new Peer("peer1", address, 30);
        Peer peer2 = new Peer("peer2", address, 30);
        peer1.join(peer2);

        peer1.subscribe(this.graph);
        peer2.subscribe(this.graph);

        peer1.execute(scheduler);
        peer2.execute(scheduler);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Set<Neighbour> peers = peer1.getPeerInfo().getNeighboursWithTimeline("peer2");

        assertEquals(true, peers.contains(new Neighbour(peer2.getPeerInfo().getHost())));

        Peer peer3 = new Peer("peer3", address, 3);
        peer3.join(peer2);
        peer3.subscribe(this.graph);
        peer3.execute(scheduler);

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        peers = peer1.getPeerInfo().getNeighboursWithTimeline("peer3");

        assertEquals(1, peers.size());
        assertEquals(true, peers.contains(new Neighbour(peer2.getPeerInfo().getHost())));
    }
}
