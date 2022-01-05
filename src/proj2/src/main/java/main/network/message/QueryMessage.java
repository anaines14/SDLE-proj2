package main.network.message;

import main.network.PeerInfo;

public class QueryMessage extends Message {
    private final String wantedTimeline;

    public QueryMessage(String username, PeerInfo peerInfo) {
        super(peerInfo);
        this.wantedTimeline = username;
    }

    public String getWantedTimeline() {
        return wantedTimeline;
    }

    @Override
    public String getType() {
        return "QUERY";
    }
}
