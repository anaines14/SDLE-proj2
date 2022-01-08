package main.model.message.request.query;

import main.model.PeerInfo;

public class SubMessage extends QueryMessageImpl {
    public static final String type = "SUB";

    public SubMessage(String username, PeerInfo peerInfo) {
        super(username, peerInfo);
    }

    public String getSubbedUser() {
        return wantedUsername;
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
