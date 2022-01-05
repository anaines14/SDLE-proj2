package main.network.message;

import main.network.neighbour.Host;

public class KoMessage extends MessageResponse {

    public KoMessage(Host host) {
        super(host);
    }

    @Override
    public String getType() {
        return "KO";
    }
}
