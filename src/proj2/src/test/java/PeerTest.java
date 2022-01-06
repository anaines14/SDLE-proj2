import main.Peer;
import main.model.neighbour.Neighbour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

public class PeerTest {
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
    public void closeWithoutHanging() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTimeout(Duration.ofSeconds(1), peer1::stop);
        assertTimeout(Duration.ofSeconds(1), peer2::stop);
        assertTimeout(Duration.ofSeconds(1), peer3::stop);
        assertTimeout(Duration.ofSeconds(1), scheduler::shutdown);
    }

    @Test
    public void peerJoining() {
        peer1.join(new Neighbour(peer3.getPeerInfo().getHost()));
        peer3.join(new Neighbour(peer2.getPeerInfo().getHost()));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Set<String> peer1Neigh = peer1.getPeerInfo().getNeighbours().stream().map(Neighbour::getUsername).collect(Collectors.toSet());
        Set<String> peer2Neigh = peer2.getPeerInfo().getNeighbours().stream().map(Neighbour::getUsername).collect(Collectors.toSet());
        Set<String> peer3Neigh = peer3.getPeerInfo().getNeighbours().stream().map(Neighbour::getUsername).collect(Collectors.toSet());

        System.out.println(peer1Neigh);
        System.out.println(peer2Neigh);
        System.out.println(peer3Neigh);

        assertEquals(new HashSet<>(Arrays.asList("u2", "u3")), peer1Neigh);
        assertEquals(new HashSet<>(Arrays.asList("u1", "u3")), peer2Neigh);
        assertEquals(new HashSet<>(Arrays.asList("u2", "u1")), peer3Neigh);

        this.close();
    }

    @Test
    public void peerCommunication() {
        peer1.join(new Neighbour(peer3.getPeerInfo().getHost()));
        peer3.join(new Neighbour(peer1.getPeerInfo().getHost()));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
