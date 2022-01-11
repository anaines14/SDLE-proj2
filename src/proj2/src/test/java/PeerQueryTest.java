import main.Peer;
import main.controller.message.MessageSender;
import main.model.timelines.Timeline;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.TestUtils;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

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
        peer1 = new Peer("u1", localhost,  5);
        peer2 = new Peer("u2", localhost, 1);
        peer3 = new Peer("u3", localhost, 10);
        peer4 = new Peer("u4", localhost, 10);
        peer5 = new Peer("u5", localhost, 20);
        scheduler = new ScheduledThreadPoolExecutor(5);

        List<Peer> peers = Arrays.asList(peer1, peer2, peer3, peer4, peer5);

        for (Peer p: peers) {
            p.execute(scheduler);
            System.out.println(p.getPeerInfo().getUsername() + ": " + p.getPeerInfo().getPort());
        }

        peer1.join(peer2);
        peer3.join(peer1);
        peer4.join(peer1);
        peer4.join(peer5);
    }

    @AfterAll
    static void cleanup() {
        TestUtils.deleteDirectory(new File("stored_timelines"));
    }

    @Test
    public void queryPeer() throws InterruptedException {
        MessageSender.addIgnoredMsg("PING");
        MessageSender.addIgnoredMsg("PONG");
        Thread.sleep(10000); // Wait for peers to add eachother as neighbours

        Timeline peer5Timeline = peer1.requestTimeline("u5");
        assertEquals(peer5.getPeerInfo().getTimelineInfo().getTimeline("u5"), peer5Timeline);
        // check if peer1 saved timeline
        peer5.stop();
        Thread.sleep(2000);
        Timeline peer5Timeline2 = peer3.requestTimeline("u5");
        assertEquals(peer5.getPeerInfo().getTimelineInfo().getTimeline("u5"), peer5Timeline2);

        peer2.stop();
        peer3.stop();
        peer4.stop();
        peer5.stop();
    }
}
