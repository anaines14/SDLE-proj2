package main.controller.message;

import main.model.PeerInfo;
import main.model.message.*;
import main.model.message.request.*;
import main.model.message.request.PongMessage;
import main.model.neighbour.Neighbour;
import main.model.timelines.Timeline;
import main.model.timelines.TimelineInfo;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.IntStream;

import static main.Peer.MAX_RANDOM_NEIGH;

// Dá handle só a mensagens que iniciam requests (PING)
public class MessageHandler {
    private final ConcurrentMap<UUID, CompletableFuture<Message>> promises;
    private final PeerInfo peerInfo;
    private final MessageSender sender;

    public MessageHandler(PeerInfo peerInfo, MessageSender sender,
                          ConcurrentMap<UUID, CompletableFuture<Message>> promises) {
        this.peerInfo = peerInfo;
        this.sender = sender;
        this.promises = promises;
    }

    public void handle(Message message) {
        if (!(message instanceof MessageRequest)) // We only can handle message requests
            return;

        handle((MessageRequest) message);
    }

    private void handle(MessageRequest message) {
        // System.out.println(peerInfo.getUsername() + " RECV[" + message.getType() + "]: " + message.getLastSender().getPort());
        switch (message.getType()) {
            case "PING":
                handle((PingMessage) message);
                return;
            case "PONG":
                handle((PongMessage) message);
                return;
            case "QUERY":
                handle((QueryMessage) message);
                return;
            case "QUERY_HIT":
                handle((QueryHitMessage) message);
                return;
            default:
        }
    }

    private void handle(PingMessage message) {
        // Reply with a Pong message with our info
        Neighbour ourInfo = new Neighbour(peerInfo.getHost());

        peerInfo.addHost(message.getSender());
        PongMessage replyMsg = new PongMessage(ourInfo, peerInfo.getHostCache(), message.getId());
        this.sender.sendRequestNTimes(replyMsg, message.getSender().getPort());
    }

    private void handle(PongMessage message) {
        // Complete future of ping request
        if (promises.containsKey(message.getId())) {
            promises.get(message.getId()).complete(message);
        }
    }

    private void handle(QueryMessage message) {
        TimelineInfo ourTimelineInfo = peerInfo.getTimelineInfo();
        String wantedUser = message.getWantedTimeline();

        if (message.isInPath(this.peerInfo))
            return; // Already redirected this message

        if (ourTimelineInfo.hasTimeline(wantedUser)) {
            // We have timeline, send query hit to initiator
            Timeline requestedTimeline = ourTimelineInfo.getTimeline(wantedUser);
            MessageRequest queryHit = new QueryHitMessage(message.getId(), requestedTimeline);
            this.sender.sendRequestNTimes(queryHit, message.getOriginalSender().getPort());
            return;
        }

        if (!message.canResend()) {
            return; // Message has reached TTL 0
        }

        // Add ourselves to the message
        message.decreaseTtl();
        message.addToPath(new Sender(this.peerInfo));

        List<Neighbour> neighbours = peerInfo.getNeighbours().stream().filter(
                n -> !message.isInPath(new Sender(n.getAddress(), n.getPort()))
        ).toList();
        // Get random N neighbours to send
        int[] randomNeighbours = IntStream.range(0, neighbours.size()).toArray();

        int i=0;
        while (i < randomNeighbours.length && i < MAX_RANDOM_NEIGH) {
            Neighbour n = neighbours.get(i);
            this.sender.sendRequestNTimes(message, n.getPort());
            ++i;
        }
    }

    private void handle(QueryHitMessage message) {
        if (promises.containsKey(message.getId())) {
            promises.get(message.getId()).complete(message);
        }
    }
}
