import main.Peer;
import main.controller.network.Broker;
import main.model.neighbour.Neighbour;
import main.model.timelines.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.Port;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;

public class RedirectPostTest {
    private Peer peer1;
    private Peer peer2;
    private Peer peer3;
    private Broker broker;
    private ScheduledThreadPoolExecutor scheduler;

    @BeforeEach
    public void setUp() {
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}
        peer1 = new Peer("u1", localhost,  20);
        peer2 = new Peer("u2", localhost, 20);
        peer3 = new Peer("u3", localhost, 10);
        scheduler = new ScheduledThreadPoolExecutor(3);

        peer2.getPeerInfo().getHost().setMaxSubCapacity(1);

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
    public void redirectPeer() {

        peer2.join(new Neighbour(peer1.getPeerInfo().getHost()));
        peer3.join(new Neighbour(peer2.getPeerInfo().getHost()));

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        peer3.requestSub("u2");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        peer1.requestSub("u2");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(peer1.isSubscribed("u2"));
        assertTrue(peer3.isSubscribed("u2"));

        peer2.addPost("Uma posta");
        peer2.addPost("Duas postas");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map<String, List<Post>> peer1Posts = peer1.getPostOfSubscriptions();
        assertTrue(peer1Posts.containsKey("u2"));

        List<Post> posts = peer1Posts.get("u2");
        assertEquals(2, posts.size());
        assertEquals(posts.get(0).getContent(), "Uma posta");
        assertEquals(posts.get(1).getContent(), "Duas postas");
        this.close();
    }
}
