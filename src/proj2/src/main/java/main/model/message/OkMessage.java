package main.model.message;

import main.model.neighbour.Host;

public class OkMessage extends MessageResponse {

    public OkMessage(Host host) {
        super(host);
    }

    @Override
    public String getType() {
        return "OK";
    }
}
