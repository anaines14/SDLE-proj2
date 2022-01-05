package main.network.message;

import main.network.PeerInfo;

public class QueryMessage extends Message {
    private final String wantedTimeline;

    public QueryMessage(String wantedTimeline, PeerInfo peerInfo) {
        super(peerInfo);
        this.wantedTimeline = wantedTimeline;
    }

    public String getWantedTimeline() {
        return wantedTimeline;
    }

    @Override
    public String getType() {
        return "QUERY";
    }
}
