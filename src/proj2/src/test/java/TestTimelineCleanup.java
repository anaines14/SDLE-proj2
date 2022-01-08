import main.Peer;
import main.controller.message.MessageSender;
import main.model.timelines.Timeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;

public class TestTimelineCleanup {
    private Peer peer1;
    private Peer peer2;
    private Peer peer3;
    private ScheduledThreadPoolExecutor scheduler;
    private static final int MAX_KEEP_TIME = 5; // time for timeline to expire (seconds)

    @BeforeEach
    public void setUp() {
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}
        peer1 = new Peer("u1", localhost,  10);
        peer2 = new Peer("u2", localhost, 20);
        peer3 = new Peer("u3", localhost, 30);
        scheduler = new ScheduledThreadPoolExecutor(5);

        List<Peer> peers = Arrays.asList(peer1, peer2, peer3);

        for (Peer p: peers) {
            p.execute(scheduler);
            System.out.println(p.getPeerInfo().getUsername() + ": " + p.getPeerInfo().getPort());
        }

        peer1.join(peer2);
        peer2.join(peer3);
        peer3.join(peer1);
    }

    @Test
    public void queryPeer() throws InterruptedException {
        MessageSender.addIgnoredMsg("PING");
        MessageSender.addIgnoredMsg("PONG");
        Thread.sleep(4000); // Wait for peers to add each other as neighbours

        peer2.getPeerInfo().getTimelineInfo().setMaxKeepTime(MAX_KEEP_TIME);

        peer1.addPost("TestingPost u1");
        peer2.queryNeighbours("u1");

        // wait for the u1's timeline to "expire"
        try{
            Thread.sleep((MAX_KEEP_TIME + 2) *1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // provoke change on u2's timelines so cleanup is executed
        peer3.addPost("TestingPost u3");
        peer2.queryNeighbours("u3");

        // wait for responses
        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // check that peer2 does not have u1's timeline
        assertNull(peer2.getPeerInfo().getTimelineInfo().getTimeline("u1"));
        assertTrue(peer2.getPeerInfo().getTimelineInfo().hasTimeline("u3"));

        peer1.stop();
        peer2.stop();
        peer3.stop();
    }
}