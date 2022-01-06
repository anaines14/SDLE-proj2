import main.Peer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PeerQueryTest {
    private Peer peer1;
    private Peer peer2;
    private Peer peer3;
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
        scheduler = new ScheduledThreadPoolExecutor(3);

        peer1.execute(scheduler);
        peer2.execute(scheduler);
        peer3.execute(scheduler);

        peer1.join(peer2);
    }

    @Test
    public void queryPeer() throws InterruptedException {
        Thread.sleep(2000); // Wait for peers to add eachother as neighbours

        assertEquals(2, peer1.getPeerInfo().getNeighbours().size());
    }
}
