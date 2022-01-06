import main.Peer;
import main.gui.GraphWrapper;
import main.model.PeerInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class GraphTest {
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

    public List<Peer> nodeFactory(int numNodes) {
        String username = "user";
        Random rand = new Random();
        List<Peer> peers = new ArrayList<>();

        int capacity = 1;

        // initiator peer
        Peer initPeer = new Peer(username + 1, address, capacity);
        initPeer.getPeerInfo().subscribe(this.graph);
        initPeer.execute(scheduler);
        peers.add(initPeer);

        for(int i = 2; i <= numNodes; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            capacity = MIN_NODE_SIZE + rand.nextInt(MAX_NODE_SIZE);
            Peer p = new Peer(username + i, address, capacity);
            p.getPeerInfo().subscribe(this.graph);
            p.execute(scheduler);
            p.join(initPeer);
            peers.add(p);
        }

        return peers;
    }

    @Test
    public void nodeView() {
        Peer peer = new Peer("username", address, 10);
        PeerInfo publisher = peer.getPeerInfo();
        publisher.subscribe(this.graph);

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void multipleNodeView() {
        List<Peer> peers = this.nodeFactory(30);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.scheduler.shutdown();

        try {
            Thread.sleep(120000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
