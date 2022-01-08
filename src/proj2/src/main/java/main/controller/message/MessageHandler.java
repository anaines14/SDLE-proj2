package main.controller.message;

import main.model.PeerInfo;
import main.model.message.*;
import main.model.message.request.*;
import main.model.message.request.query.QueryMessage;
import main.model.message.request.query.QueryMessageImpl;
import main.model.message.request.query.SubMessage;
import main.model.message.response.*;
import main.model.message.response.query.QueryHitMessage;
import main.model.message.response.query.SubHitMessage;
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
    private final ConcurrentMap<UUID, CompletableFuture<MessageResponse>> promises;
    private final PeerInfo peerInfo;
    private final MessageSender sender;

    public MessageHandler(PeerInfo peerInfo, MessageSender sender,
                          ConcurrentMap<UUID, CompletableFuture<MessageResponse>> promises) {
        this.peerInfo = peerInfo;
        this.sender = sender;
        this.promises = promises;
    }

    public void handle(Message message) {
//         System.out.println(peerInfo.getUsername() + " RECV[" + message.getType() + "]: ");
        switch (message.getType()) {
            case "PING" -> handle((PingMessage) message);
            case "PONG" -> handle((PongMessage) message);
            case "QUERY" -> handle((QueryMessage) message);
            case "QUERY_HIT" -> handle((QueryHitMessage) message);
            case "PASSOU_BEM" -> handle((PassouBem) message);
            case "PASSOU_BEM_RESPONSE" -> handle((PassouBemResponse) message);
            case "SUB" -> handle((SubMessage) message);
            case "SUB_HIT" -> handle((SubHitMessage) message);
            default -> {
            }
        }
    }

    private void handle(PingMessage message) {
        // Reply with a Pong message with our info
        peerInfo.addHost(message.getSender());
        Neighbour ourInfo = new Neighbour(peerInfo.getHost());
        boolean isNeighbour = peerInfo.hasNeighbour(new Neighbour(message.getSender()));

        PongMessage replyMsg = new PongMessage(ourInfo, peerInfo.getHostCache(), message.getId(), isNeighbour);
        this.sender.sendMessageNTimes(replyMsg, message.getSender().getPort());
    }

    private void handle(PongMessage message) {
        // Complete future of ping request
        if (promises.containsKey(message.getId())) {
            promises.get(message.getId()).complete(message);
        }
    }

    private void propagateQueryMessage(QueryMessageImpl message) {
        if (!message.canResend()) {
            return; // Message has reached TTL 0
        }

        // Add ourselves to the message
        message.decreaseTtl();
        message.addToPath(new Sender(this.peerInfo));

        List<Neighbour> neighbours = peerInfo.getNeighbours().stream().filter(
                n -> !message.isInPath(new Sender(n.getAddress(), n.getPort()))
        ).toList();
        System.out.print(this.peerInfo.getUsername() + " SENDING TO: ");
        for (Neighbour n: neighbours)
            System.out.print(n.getUsername() + " ");
        System.out.println();
        // Get random N neighbours to send
        int[] randomNeighbours = IntStream.range(0, neighbours.size()).toArray();
        int i=0;
        while (i < randomNeighbours.length && i < MAX_RANDOM_NEIGH) {
            Neighbour n = neighbours.get(i);
            this.sender.sendMessageNTimes(message, n.getPort());
            ++i;
        }
    }

    private void handle(QueryMessage message) {
        TimelineInfo ourTimelineInfo = peerInfo.getTimelineInfo();
        String wantedUser = message.getWantedTimeline();

        if (message.isInPath(this.peerInfo))
            return; // Already redirected this message

        if (ourTimelineInfo.hasTimeline(wantedUser)) { // TODO Add this to cache so that we don't resend a response
            // We have timeline, send query hit to initiator
            Timeline requestedTimeline = ourTimelineInfo.getTimeline(wantedUser);
            MessageResponse queryHit = new QueryHitMessage(message.getId(), requestedTimeline);
            this.sender.sendMessageNTimes(queryHit, message.getOriginalSender().getPort());
            return;
        }

        this.propagateQueryMessage(message);
    }

    private void handle(QueryHitMessage message) {
        if (promises.containsKey(message.getId())) {
            promises.get(message.getId()).complete(message);
        }
    }

    private void handle(PassouBem message) {
        boolean neighboursFull = this.peerInfo.areNeighboursFull();

        boolean accepted = false;
        if (!neighboursFull) {
            this.peerInfo.addNeighbour(new Neighbour(message.getSender()));
            accepted = true;
        } else {
            Neighbour toReplace = this.peerInfo.acceptNeighbour(message.getSender());
            boolean canReplace = toReplace != null;
            if (canReplace) {
                accepted = true;
                peerInfo.replaceNeighbour(toReplace, new Neighbour(message.getSender()));
            }
        }
        PassouBemResponse response = new PassouBemResponse(message.getId(), peerInfo.getHostCache(), accepted);
        this.sender.sendMessageNTimes(response, message.getSender().getPort());
    }

    private void handle(PassouBemResponse message) {
        if (promises.containsKey(message.getId())) {
            promises.get(message.getId()).complete(message);
        }
        this.peerInfo.updateHostCache(message.getHostCache());
    }

    private void handle(SubMessage message) {
        String wantedUser = message.getWantedSub();

        if (message.isInPath(this.peerInfo))
            return; // Already redirected this message

        // this is wanted sub
        if (wantedUser.equals(this.peerInfo.getUsername())) { // TODO Add this to cache so that we don't resend a response
            // We have timeline, send query hit to initiator
            MessageResponse queryHit = new SubHitMessage(message.getId());
            this.sender.sendMessageNTimes(queryHit, message.getOriginalSender().getPort());
            return;
        }
        this.propagateQueryMessage(message);
    }

    private void handle(SubHitMessage message) {
        if (promises.containsKey(message.getId())) {
            promises.get(message.getId()).complete(message);
        }
    }
}
