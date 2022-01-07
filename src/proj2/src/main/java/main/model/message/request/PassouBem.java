package main.model.message.request;

import main.model.neighbour.Host;

public class PassouBem extends MessageRequest {
    private Host sender;

    public PassouBem(Host sender) {
        this.sender = sender;
    }

    public Host getSender() {
        return sender;
    }

    @Override
    public String getType() {
        return "PASSOU_BEM";
    }
}
