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
        this.address = peerInfo.address;
        this.port = peerInfo.port;
        this.peerInfo = peerInfo;
        this.sender = sender;
    }

    public Message handle(Message message) {
        if (!(message instanceof MessageRequest)) // We only can handle message requests
            return new KoMessage(peerInfo);

        return handle((MessageRequest) message);
    }

    private MessageResponse handle(MessageRequest message) {
        switch (message.getType()) {
            case "PING":
                return handle((PingMessage) message);

            default:
                return null;
        }
    }

    private MessageResponse handle(PingMessage message) {
        // Reply with a Pong message with our info
        Neighbour ourInfo = new Neighbour(peerInfo.username, peerInfo.address, peerInfo.port,
                peerInfo.capacity, peerInfo.getDegree(), peerInfo.getStoredTimelines());

        PongMessage replyMsg = new PongMessage(this.peerInfo, ourInfo);
        return replyMsg;
    }
}
