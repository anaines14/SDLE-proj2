package main.network.message;

import main.network.PeerInfo;

public class PingMessage extends Message {
    public PingMessage(PeerInfo info) {
        super(info);
    }

    @Override
    public String getType() {
        return "PING";
    }
}
