package main.network.message;

import main.network.PeerInfo;

public class KoMessage extends MessageResponse {
    public KoMessage(PeerInfo info) {
        super(info);
    }

    @Override
    public String getType() {
        return "KO";
    }
}
