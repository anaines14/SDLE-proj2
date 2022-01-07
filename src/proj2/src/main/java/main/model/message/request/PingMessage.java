package main.model.message.request;

import main.model.neighbour.Host;

public class PingMessage extends MessageRequest {
    private Host sender;

    public PingMessage(Host host) {
        super();
        this.sender = host;
    }

    public Host getSender() {
        return sender;
    }

    @Override
    public String getType() {
        return "PING";
    }
}
