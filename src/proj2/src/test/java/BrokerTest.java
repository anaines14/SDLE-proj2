import main.controller.network.Authenticator;
import main.controller.network.Broker;
import main.model.PeerInfo;
import main.controller.message.MessageSender;
import main.model.SocketInfo;
import main.model.message.request.PingMessage;
import main.model.neighbour.Host;
import main.model.timelines.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zeromq.ZContext;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BrokerTest {
    Broker broker;
    MessageSender sender;
    PeerInfo peerInfo;

    InetAddress localhost;
    private ZContext context;

    @BeforeEach
    public void setUp() {
        localhost = null;
        try {
            localhost = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}

        context = new ZContext();
        MessageSender sender1 = new MessageSender("user1", "8083", 3, 500, context);
        Authenticator authenticator = new Authenticator(context);
        this.broker = new Broker(context, localhost, authenticator);
        this.peerInfo = new PeerInfo("user1", localhost, 3, this.broker.getSocketInfo());
        this.broker.setSender(sender1);
        this.broker.setPeerInfo(peerInfo);

        broker.execute();
        this.sender = new MessageSender("user2", peerInfo.getPort(), 3, 500, context);
    }

    @Test
    public void testBroker() {
        SocketInfo socketInfo = new SocketInfo("8002", "8001");
        Host peer2 = new Host("user2", localhost, 10, 10, 3, 3, socketInfo);
        assertTrue(sender.sendMessageNTimes(new PingMessage(peer2), peerInfo.getPort()));
        broker.stop();
    }

    @Test
    public void publish() {
        // Create a new broker that subscribes to the original broker
        ZContext ctx = new ZContext();
        Authenticator authenticator = new Authenticator(ctx);
        Broker broker2 = new Broker(ctx, localhost, authenticator);
        PeerInfo peerInfo2 = new PeerInfo("user2", localhost, 3, broker2.getSocketInfo());
        MessageSender sender2 = new MessageSender(peerInfo2, 3, 500, ctx);
        broker2.setSender(sender2);
        broker2.setPeerInfo(peerInfo2);
        broker2.execute();

        broker2.subscribe(peerInfo.getUsername(), peerInfo.getAddress(), peerInfo.getPublishPort());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // If broker publishes a post, broker must be able to see it
        Post carlos = new Post(0, "Carlos");
        broker.publishPost(carlos);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, broker2.popSubMessages().size());
    }
}
