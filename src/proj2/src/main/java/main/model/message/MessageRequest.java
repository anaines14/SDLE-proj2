package main.model.message;

import main.model.neighbour.Host;

public abstract class MessageRequest extends Message {
    public MessageRequest(Host host) {
        super(host);
    }
}
