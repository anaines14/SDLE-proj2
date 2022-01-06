import main.Peer;
import main.gui.GraphWrapper;
import main.model.PeerInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class GraphTest {
    private GraphWrapper graph;
    private InetAddress address;
    private ScheduledThreadPoolExecutor scheduler;

    @BeforeEach
    public void setUp() {
        scheduler = new ScheduledThreadPoolExecutor(5);
        this.graph = new GraphWrapper("Network");

        address = null;
        try {
            address = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}

        this.graph.display();
    }

    public void nodeFactory(int numNodes) {
        String username = "user";
        Random rand = new Random();

        // initiator peer
        Peer initPeer = new Peer(username + 1, address, rand.nextInt());
        initPeer.getPeerInfo().subscribe(this.graph);
        initPeer.execute(scheduler);

        for(int i = 2; i <= numNodes; i++) {
            Peer p = new Peer(username + i, address, rand.nextInt());
            p.getPeerInfo().subscribe(this.graph);
            p.execute(scheduler);
            p.join(initPeer);
        }

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
        this.nodeFactory(10);

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
