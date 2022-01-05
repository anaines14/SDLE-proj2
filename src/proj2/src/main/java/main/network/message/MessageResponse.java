package main.network.message;

import main.network.PeerInfo;

public abstract class MessageResponse extends Message {
    public MessageResponse(PeerInfo info) {
        super(info);
    }
}
