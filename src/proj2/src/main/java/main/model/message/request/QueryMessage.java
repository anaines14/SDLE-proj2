package main.model.message.request;

import main.model.PeerInfo;

// Message that will be redirected
public class QueryMessage extends MessageRequest {
    public static final String type = "QUERY";
    private final static int TTL = 10; // TODO Param this
    public Path path;
    private final String wantedUsername;
    private int timeToLive;

    public QueryMessage(String username, PeerInfo peerInfo) {
        super();
        this.path = new Path();
        this.path.addSender(new Sender(peerInfo));
        // We put username in the beginning of the id so that no identifier is the same
        this.wantedUsername = username;
        this.timeToLive = TTL;
    }

    public boolean isInPath(Sender sender) {
        return this.path.isInPath(sender);
    }

    public boolean isInPath(PeerInfo peerInfo) {
        return this.isInPath(new Sender(peerInfo));
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
        return type;
    }

    @Override
    public String toString() {
        return super.toString() + "("+ this.wantedUsername + ":" + path.toString() + ")";
    }
}
