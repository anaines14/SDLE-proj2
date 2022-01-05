package main.network.message;

import main.network.neighbour.Host;

public abstract class MessageResponse extends Message {
    public MessageResponse(Host host) {
        super(host);
    }
}
