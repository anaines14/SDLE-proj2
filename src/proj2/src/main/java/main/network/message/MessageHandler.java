package main.network.message;

import main.network.PeerInfo;
import main.network.neighbour.Neighbour;

import java.net.InetAddress;

// Dá handle só a mensagens que iniciam requests (PING)
public class MessageHandler {
    private InetAddress address;
    private String port;
    private PeerInfo peerInfo;
    private MessageSender sender;

    public MessageHandler(PeerInfo peerInfo, MessageSender sender) {
        this.address = peerInfo.getAddress();
        this.port = peerInfo.getPort();
        this.peerInfo = peerInfo;
        this.sender = sender;
    }

    public Message handle(Message message) {
        if (!(message instanceof MessageRequest)) // We only can handle message requests
            return new KoMessage(peerInfo.getHost());

        return handle((MessageRequest) message);
    }

    private MessageResponse handle(MessageRequest message) {
        // System.out.println(peerInfo.getUsername() + " RECV[" + message.getType() + "]: " + message.getLastSender().getPort());
        switch (message.getType()) {
            case "PING":
                return handle((PingMessage) message);
            default:
                return null;
        }
    }

    private MessageResponse handle(PingMessage message) {
        // Reply with a Pong message with our info
        Neighbour ourInfo = new Neighbour(peerInfo.getHost());

        peerInfo.addHost(message.getSender());

        PongMessage replyMsg = new PongMessage(ourInfo, peerInfo.getHostCache());
        return replyMsg;
    }

    private void handle(QueryMessage message) {
        // TODO
        System.out.println("Received QueryMessage");
    }
}
