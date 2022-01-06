package main.model.message;

import main.model.neighbour.Host;

public class KoMessage extends MessageResponse {

    public KoMessage(Host host) {
        super(host);
    }

    @Override
    public String getType() {
        return "KO";
    }
}
