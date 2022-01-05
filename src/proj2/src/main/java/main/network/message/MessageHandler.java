package main.network.message;

import main.network.PeerInfo;
import main.network.neighbour.Neighbour;
import org.zeromq.ZContext;

import java.net.InetAddress;

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
        switch (message.getType()) {
            case "PING":
                return handle((PingMessage) message);
            case "PONG":
                return handle((PongMessage) message);

            default:
                return null;
        }
    }

    public Message handle(PingMessage message) {
        // Reply with a Pong message with our info
        Neighbour ourInfo = new Neighbour(peerInfo.username, peerInfo.address, peerInfo.port,
                peerInfo.capacity, peerInfo.getDegree(), peerInfo.getStoredTimelines());

        PongMessage replyMsg = new PongMessage(this.peerInfo, ourInfo);
        return replyMsg;
    }

    public Message handle(PongMessage message) {
        // Update/Add info that we have about a peer
        Neighbour responder = message.sender;
        if (peerInfo.hasNeighbour(responder))
            peerInfo.updateNeighbour(responder);
        else
            peerInfo.addNeighbour(responder);

        return new OkMessage(this.peerInfo);
    }
}
