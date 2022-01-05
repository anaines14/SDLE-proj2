package main.network.message;

import main.network.neighbour.Host;

public class PingMessage extends MessageRequest {
    private Host sender;

    public PingMessage(Host host) {
        super(host);
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
