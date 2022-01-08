import main.controller.network.Broker;
import main.model.PeerInfo;
import main.controller.message.MessageSender;
import main.model.message.request.MessageRequest;
import main.model.message.request.PingMessage;
import main.model.neighbour.Host;
import org.junit.jupiter.api.Test;
import org.zeromq.ZContext;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BrokerTest {

    @Test
    public void testBroker() {
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}

        ZContext context = new ZContext();
        PeerInfo peerInfo = new PeerInfo("user1", localhost, 3, "8003", "8003");
        MessageSender sender1 = new MessageSender("user1", "8083", 3, 500, context);
        Broker broker = new Broker(context, sender1, peerInfo);

        Host peer2 = new Host("user2", localhost, "8002", "8003", 10, 10);
        broker.execute();

        MessageRequest request = new PingMessage(peer2);
        MessageSender sender2 = new MessageSender("user2", "8080", 3, 500, context);
        MessageSender sender3 = new MessageSender("user4", "8081",3, 500, context);
        assertTrue(sender2.sendRequestNTimes(new PingMessage(peer2), peerInfo.getPort()));

        broker.stop();
    }
}
