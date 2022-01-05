package main.network.message;

import main.network.PeerInfo;

public abstract class MessageRequest extends Message {
    public MessageRequest(PeerInfo info) {
        super(info);
    }
}
