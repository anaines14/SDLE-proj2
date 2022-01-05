package main.network.message;

import main.network.neighbour.Host;

public class QueryMessage extends Message {
    private final String wantedTimeline;

    public QueryMessage(String wantedTimeline, Host host) {
        super(host);
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
