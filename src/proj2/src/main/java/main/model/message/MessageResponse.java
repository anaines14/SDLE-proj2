package main.model.message;

import main.model.neighbour.Host;

public abstract class MessageResponse extends Message {
    public MessageResponse(Host host) {
        super(host);
    }
}
