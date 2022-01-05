package main.network.message;

import main.network.PeerInfo;

public class OkMessage extends Message {
    public OkMessage(PeerInfo info) {
        super(info);
    }

    @Override
    public String getType() {
        return "OK";
    }
}
