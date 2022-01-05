import main.network.Peer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertTimeout;

public class PeerTest {
    private Peer peer1;
    private Peer peer2;
    private ScheduledThreadPoolExecutor scheduler;

    @BeforeEach
    public void setUp() {
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}
        peer1 = new Peer("u1", localhost, "8100", 10);
        peer2 = new Peer("u2", localhost, "8101", 20);
        scheduler = new ScheduledThreadPoolExecutor(3);

        peer1.execute(scheduler);
        peer2.execute(scheduler);
    }

    @Test
    public void closeWithoutHanging() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTimeout(Duration.ofSeconds(1), peer1::stop);
        assertTimeout(Duration.ofSeconds(1), peer2::stop);
        assertTimeout(Duration.ofSeconds(1), scheduler::shutdown);
    }
}
