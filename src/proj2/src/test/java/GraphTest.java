import main.Peer;
import main.controller.message.MessageSender;
import main.gui.GraphWrapper;
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
        MessageSender.addIgnoredMsg("PING");
        MessageSender.addIgnoredMsg("PONG");
        MessageSender.addIgnoredMsg("PASSOU_BEM");
        MessageSender.addIgnoredMsg("PASSOU_BEM_RESPONSE");

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

        int capacity = 15;

        // initiator peer
        Peer initPeer = new Peer(username + 1, address, capacity);
        initPeer.subscribe(this.graph);
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
            p.subscribe(this.graph);
            p.execute(scheduler);
            p.join(initPeer);
            peers.add(p);
        }

        return peers;
    }

    @Test
    public void nodeView() {
        Peer peer = new Peer("username", address, 10);
        peer.subscribe(this.graph);

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void multipleNodeViewWith4() {
        // MessageSender.addIgnoredMsg("PING");
        // MessageSender.addIgnoredMsg("PONG");
        //MessageSender.addIgnoredMsg("PASSOU_BEM");
        //MessageSender.addIgnoredMsg("PASSOU_BEM_RESPONSE");

        List<Peer> peers = this.nodeFactory(5);

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.scheduler.shutdown();

        for (int i=0; i<peers.size(); ++i) {
            System.out.println(peers.get(i).getPeerInfo().getHostCache() + " " + peers.get(i).getPeerInfo().getDegree());
        }

        try {
            Thread.sleep(420000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void multipleNodeView() {
        List<Peer> peers = this.nodeFactory(3);

        try {
            Thread.sleep(3000);
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

    @Test
    public void messageView() {
        List<Peer> peers = this.nodeFactory(4);
        Peer peer1 = peers.get(0);
        Peer peer2 = peers.get(1);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        peer1.requestTimeline(peer2.getPeerInfo().getUsername());

        try {
            Thread.sleep(6000);
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

    @Test
    public void testEdges() {
        Peer peer1 = new Peer("user1", address, 50);
        Peer peer2 = new Peer("user2", address, 20);
        Peer peer3 = new Peer("user3", address, 3);

        peer1.subscribe(this.graph);
        peer2.subscribe(this.graph);
        peer3.subscribe(this.graph);

        peer2.join(peer1);
        peer3.join(peer2);

        peer1.execute(scheduler);
        peer2.execute(scheduler);
        peer3.execute(scheduler);

        try {
            Thread.sleep(120000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
