package main.model.message.request;

import main.model.PeerInfo;

import java.util.UUID;

// Message that will be redirected
public class QueryMessage extends MessageRequest {
    private final static int TTL = 10;

    public Path path;
    private final String id;
    private final String wantedUsername;
    private int timeToLive;

    public QueryMessage(String username, PeerInfo peerInfo) {
        this.path = new Path();
        this.path.addSender(new Sender(peerInfo));
        // We put username in the beginning of the id so that no identifier is the same
        this.id = peerInfo.getUsername() + ": " + UUID.randomUUID();
        this.wantedUsername = username;
        this.timeToLive = TTL;
    }

    public boolean isInPath(PeerInfo peerInfo) {
        Sender lookFor = new Sender(peerInfo);
        return this.path.isInPath(lookFor);
    }

    public void addToPath(Sender sender) {
        path.addSender(sender);
    }

    public boolean canResend() {
        return timeToLive != 0;
    }

    public void decreaseTtl() {
        if (canResend()) timeToLive--;
    }

    public String getWantedTimeline() {
        return wantedUsername;
    }

    public Sender getLastSender() {
        return path.getLastSender();
    }

    public Sender getOriginalSender() {
        return path.getOriginalSender();
    }

    @Override
    public String getType() {
        return "QUERY";
    }

    @Override
    public String toString() {
        return super.toString() + "("+ this.wantedUsername + ":" + path.toString() + ")";
    }
}
