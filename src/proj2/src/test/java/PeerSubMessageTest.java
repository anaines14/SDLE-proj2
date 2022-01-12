import main.Peer;
import main.controller.message.MessageSender;
import main.model.neighbour.Neighbour;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.TestUtils;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;

public class PeerSubMessageTest {
    private Peer peer1;
    private Peer peer2;
    private Peer peer3;
    private ScheduledThreadPoolExecutor scheduler;

    @BeforeEach
    public void setUp() {
        MessageSender.addIgnoredMsg("PING");
        MessageSender.addIgnoredMsg("PONG");
        MessageSender.addIgnoredMsg("PASSOU_BEM");
        MessageSender.addIgnoredMsg("PASSOU_BEM_RESPONSE");
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

    @AfterAll
    static void cleanup() {
        TestUtils.deleteDirectory(new File("stored_timelines"));
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
        peer2.requestSub("u3");
        peer1.requestSub("u3");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(peer1.isSubscribed("u2"));
        assertTrue(peer1.isSubscribed("u3"));

        peer2.addPost("Uma posta");
        peer3.addPost("Duas postas");
        peer2.addPost("Duas postas");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("P1: " + peer1.getPostOfSubscriptions());
        System.out.println("P2: " + peer2.getPostOfSubscriptions());

        this.close();
    }
}
