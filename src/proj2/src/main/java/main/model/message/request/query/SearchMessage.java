package main.model.message.request.query;

import main.model.PeerInfo;

public class SearchMessage extends QueryMessageImpl{
    public static final String type = "SEARCH";

    public SearchMessage(String search, PeerInfo peerInfo) { super(search, peerInfo); }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type + "("+ this.wantedSearch + ":" + path.toString() + ")";
    }
}
