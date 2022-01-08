import main.controller.network.Broker;
import main.model.PeerInfo;
import main.controller.message.MessageSender;
import main.model.message.request.MessageRequest;
import main.model.message.request.PingMessage;
import main.model.neighbour.Host;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
        this.broker = new Broker(context, localhost);
        peerInfo = new PeerInfo("user1", localhost, 3, broker.getFrontendPort(), broker.getPublisherPort());
        this.broker.setSender(sender1);
        this.broker.setPeerInfo(peerInfo);

        broker.execute();
        this.sender = new MessageSender("user2", peerInfo.getPort(), 3, 500, context);
    }

    @Test
    public void testBroker() {
        Host peer2 = new Host("user2", localhost, "8002", "8003", 10, 10);
        assertTrue(sender.sendRequestNTimes(new PingMessage(peer2), peerInfo.getPort()));

        broker.stop();
    }

    @Test
    public void subscribe() {
        ZMQ.Socket subscribe = context.createSocket(SocketType.SUB);
        subscribe.connect("tcp://" + peerInfo.getAddress().getHostName() + ":" + peerInfo.getPublishPort());
    }
}
