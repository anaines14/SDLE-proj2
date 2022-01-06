import main.Peer;
import main.model.neighbour.Neighbour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PeerQueryTest {
    private Peer peer1;
    private Peer peer2;
    private Peer peer3;
    private Peer peer4;
    private Peer peer5;
    private ScheduledThreadPoolExecutor scheduler;

    @BeforeEach
    public void setUp() {
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}
        peer1 = new Peer("u1", localhost,  10);
        peer2 = new Peer("u2", localhost, 20);
        peer3 = new Peer("u3", localhost, 30);
        peer4 = new Peer("u4", localhost, 30);
        peer5 = new Peer("u5", localhost, 50);
        scheduler = new ScheduledThreadPoolExecutor(3);

        peer1.execute(scheduler);
        peer2.execute(scheduler);
        peer3.execute(scheduler);
        peer4.execute(scheduler);
        peer5.execute(scheduler);

        peer1.join(peer2);
        peer3.join(peer1);
        peer4.join(peer1);
        peer4.join(peer5);
    }

    @Test
    public void queryPeer() throws InterruptedException {
        Thread.sleep(20000); // Wait for peers to add eachother as neighbours

        Set<String> peer1Neigh = peer1.getPeerInfo().getNeighbours().stream().map(Neighbour::getUsername).collect(Collectors.toSet());

        assertEquals(2, peer1.getPeerInfo().getNeighbours().size());
        peer1.queryNeighbours("u5");

        peer1.stop();
        peer2.stop();
        peer3.stop();
        peer4.stop();
        peer5.stop();
    }
}
