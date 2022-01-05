package main.network.message;

import main.network.neighbour.Host;

public abstract class MessageRequest extends Message {
    public MessageRequest(Host host) {
        super(host);
    }
}
