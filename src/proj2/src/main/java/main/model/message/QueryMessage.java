package main.model.message;

import main.model.neighbour.Host;

public class QueryMessage extends MessageRequest {
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
