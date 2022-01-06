import main.controller.network.Broker;
import main.model.PeerInfo;
import main.controller.message.MessageSender;
import main.model.message.Message;
import main.model.message.PingMessage;
import main.model.message.PongMessage;
import main.model.neighbour.Host;
import org.junit.jupiter.api.Test;
import org.zeromq.ZContext;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrokerTest {

    @Test
    public void testBroker() {
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}

        ZContext context = new ZContext();
        MessageSender sender = new MessageSender(localhost, "8001", "user1", context);
        PeerInfo peerInfo = new PeerInfo(localhost, "8001", "user1", 50);
        Broker broker = new Broker(context, sender, peerInfo);

        Host peer2 = new Host("user2", localhost, "8002", 10, 10);
        broker.execute();

        Message request = new PingMessage(peer2);
        MessageSender sender2 = new MessageSender(localhost, "8002", "user2", context);

        MessageSender sender3 = new MessageSender(localhost, "8002", "user4", context);
        assertEquals(PongMessage.class, sender2.sendRequest(request, "8001", 500).getClass());
        assertEquals(PongMessage.class, sender3.sendRequest(request, "8001", 500).getClass());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        broker.close();
    }
}
