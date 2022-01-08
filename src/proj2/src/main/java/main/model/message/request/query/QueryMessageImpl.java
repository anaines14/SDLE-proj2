package main.model.message.request.query;

import main.model.PeerInfo;
import main.model.message.request.MessageRequest;
import main.model.message.request.Path;
import main.model.message.request.Sender;


public abstract class QueryMessageImpl extends MessageRequest {
    private final static int TTL = 10;
    public Path path;
    protected final String wantedUsername;
    protected int timeToLive;

    public QueryMessageImpl(String username, PeerInfo peerInfo) {
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

    public Sender getLastSender() {
        return path.getLastSender();
    }

    public Sender getOriginalSender() {
        return path.getOriginalSender();
    }

    @Override
    public String getType() {
        System.err.println("Query Message Implementation.");
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + "("+ this.wantedUsername + ":" + path.toString() + ")";
    }
}
