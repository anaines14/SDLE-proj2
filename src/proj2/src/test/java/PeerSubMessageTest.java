import main.Peer;
import main.model.neighbour.Neighbour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

public class PeerSubMessageTest {
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
    }

    public void close() {
        peer1.stop();
        peer2.stop();
        peer3.stop();
    }

    @Test
    public void subToPeer() {
        peer1.join(new Neighbour(peer3.getPeerInfo().getHost()));
        peer3.join(new Neighbour(peer2.getPeerInfo().getHost()));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        peer1.requestSub("u2");

        this.close();
    }
}
