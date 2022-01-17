package main.model.message.request;

import main.model.neighbour.Host;

public class SubPing extends MessageRequest {
    private Host sender;
    public SubPing(Host sender) {
        this.sender = sender;
    }

    public Host getSender() {
        return sender;
    }

    @Override
    public String getType() {
        return "SUB_PING";
    }
}
