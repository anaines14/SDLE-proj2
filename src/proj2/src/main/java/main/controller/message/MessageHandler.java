package main.controller.message;

import main.controller.network.Authenticator;
import main.model.PeerInfo;
import main.model.SocketInfo;
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

import java.security.PublicKey;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.IntStream;

import static main.Peer.MAX_RANDOM_NEIGH;

// Dá handle só a mensagens que iniciam requests (PING)
public class MessageHandler {
    private final ConcurrentMap<UUID, CompletableFuture<MessageResponse>> promises;
    private PeerInfo peerInfo;
    private final SocketInfo socketInfo;
    private Authenticator authenticator;
    private MessageSender sender;

    public MessageHandler(ConcurrentMap<UUID, CompletableFuture<MessageResponse>> promises,
                          SocketInfo socketInfo, Authenticator authenticator) {
        this.peerInfo = null;
        this.sender = null;
        this.socketInfo = socketInfo;
        this.authenticator = authenticator;
        this.promises = promises;
    }

    public void setSender(MessageSender sender) {
        this.sender = sender;
    }

    public void setPeerInfo(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
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
        Neighbour ourInfo = new Neighbour(this.peerInfo.getHost(), this.peerInfo.getTimelinesFilter());
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

        Set<Neighbour> ngbrsToReceive = peerInfo.getNeighbours();
        if (this.peerInfo.isSuperPeer()) // super peer => use bloom filter
            ngbrsToReceive = this.peerInfo.getNeighboursWithTimeline(message.getWantedUsername());

        List<Neighbour> neighbours = ngbrsToReceive.stream().filter(
                n -> !message.isInPath(new Sender(n.getAddress(), n.getPort())))
                .toList();

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

            if (peerInfo.isAuth())
                requestedTimeline.addSignature(peerInfo.getPrivateKey());

            MessageResponse queryHit = new QueryHitMessage(message.getId(), requestedTimeline);
            this.sender.sendMessageNTimes(queryHit, message.getOriginalSender().getPort());
            return;
        }

        this.propagateQueryMessage(message);
    }

    private void handle(QueryHitMessage message) {
        if (promises.containsKey(message.getId())) {
            if (message.getTimeline().hasSignature() && peerInfo.isAuth()) {
                // Timeline is signed and we can verify it
                String username = message.getTimeline().getUsername();
                PublicKey publicKey = authenticator.requestPublicKey(username);
                assert(publicKey != null);
                if (!message.getTimeline().verifySignature(publicKey))
                    return; // Don't accept message
            }

            promises.get(message.getId()).complete(message);
        }
    }

    private void handle(PassouBem message) {
        boolean neighboursFull = this.peerInfo.areNeighboursFull();
        boolean accepted = false;
        if (!neighboursFull) {
            accepted = true;
            this.peerInfo.addNeighbour(new Neighbour(message.getSender()));
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

        if (peerInfo.canAcceptSub()) {
            if (wantedUser.equals(this.peerInfo.getUsername())) { // TODO Add this to cache so that we don't resend a response
                // We are the requested sub, send query hit to initiator
                MessageResponse subHit = new SubHitMessage(message.getId(), this.peerInfo.getPort(),
                        this.peerInfo.getPublishPort(), this.peerInfo.getAddress());
                this.sender.sendMessageNTimes(subHit, message.getOriginalSender().getPort());
                peerInfo.addSubscriber(message.getOriginalSender().getPort());
                return;
            } else if (this.peerInfo.hasSubscription(wantedUser)) {
                // TODO: create socket and add to redirects
                String redirectPubPort = this.socketInfo.addRedirect(wantedUser, this.peerInfo.getAddress());

                // We are subbed to the requested sub, send query hit to initiator
                MessageResponse subHit = new SubHitMessage(message.getId(), this.peerInfo.getPort(),
                        redirectPubPort, this.peerInfo.getAddress());
                this.sender.sendMessageNTimes(subHit, message.getOriginalSender().getPort());
                // add subscriber to this peer
                peerInfo.addRedirect(wantedUser, message.getOriginalSender().getPort());
                return;
            }
        }

        this.propagateQueryMessage(message);
    }

    private void handle(SubHitMessage message) {
        if (promises.containsKey(message.getId())) {
            promises.get(message.getId()).complete(message);
        }
    }
}
