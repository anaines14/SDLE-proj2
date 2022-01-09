import main.Peer;
import main.controller.message.MessageSender;
import main.gui.GraphWrapper;
import main.model.neighbour.Neighbour;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ViewTest {
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
    public void multipleNodeView() {
       this.nodeFactory(3);

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
    public void edgesView() {
        Peer peer1 = new Peer("BIG", address, 50);
        Peer peer2 = new Peer("Med", address, 20);
        Peer peer3 = new Peer("small", address, 3);

        System.out.println(peer1.getPeerInfo().getPort());
        System.out.println(peer2.getPeerInfo().getPort());
        System.out.println(peer3.getPeerInfo().getPort());

        peer1.subscribe(this.graph);
        peer2.subscribe(this.graph);
        peer3.subscribe(this.graph);

        peer2.join(peer1);
        peer3.join(peer2);

        peer1.execute(scheduler);
        peer2.execute(scheduler);
        peer3.execute(scheduler);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(peer1.getPeerInfo().getNeighbours());
        System.out.println(peer2.getPeerInfo().getNeighbours());
        System.out.println(peer3.getPeerInfo().getNeighbours());

        try {
            Thread.sleep(120000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTimelineView() {
        List<Peer> peers = this.nodeFactory(4);
        Peer peer1 = peers.get(0);

        for (Peer peer: peers) { // Make posts
            peer.addPost("Hello! Im peer" + peer.getPeerInfo().getUsername());
        }
        peer1.addPost("Goodbye!");

        // Get timelines
        for (int i = 1; i < peers.size(); i++)
            peer1.requestTimeline(peers.get(i).getPeerInfo().getUsername());

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // make sure peer has timelines
        for (int i = 1; i < peers.size(); i++)
            assertTrue(peer1.getPeerInfo().getTimelineInfo().hasTimeline(peers.get(i).getPeerInfo().getUsername()));

        peer1.showFeed();
    }

    @Test
    public void subscriptionView() {
        List<Peer> peers = setUpSubscriptions();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Peer peer1 = peers.get(0);
        peer1.requestSub("u2");
        peer1.requestSub("u3");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void redirectView() {
    }

    private List<Peer> setUpSubscriptions() {
        List<Peer> peers = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Peer peer = new Peer("u" + i, address,  10 * i);
            peers.add(peer);
            peer.subscribe(this.graph);
        }

        peers.get(0).join(new Neighbour(peers.get(2).getPeerInfo().getHost()));
        peers.get(2).join(new Neighbour(peers.get(1).getPeerInfo().getHost()));

        for (Peer peer: peers)
            peer.execute(scheduler);

        return peers;
    }
}
