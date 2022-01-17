package main.model.message.request.query;

import main.model.PeerInfo;

// Message that will be redirected
public class QueryMessage extends QueryMessageImpl {
    public static final String type = "QUERY";

    public QueryMessage(String username, PeerInfo peerInfo) {
        super(username, peerInfo);
    }

    public String getWantedTimeline() {
        return wantedSearch;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return super.toString() + "("+ this.wantedSearch + ":" + path.toString() + ")";
    }
}
