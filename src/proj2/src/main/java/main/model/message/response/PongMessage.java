package main.model.message.response;

import main.model.neighbour.Host;
import main.model.neighbour.Neighbour;

import java.util.Set;

public class PongMessage extends MessageResponse {
    public Neighbour sender;
    public Set<Host> hostCache;

    public PongMessage(Neighbour sender, Set<Host> hostCache) {
        this.sender = sender;
        this.hostCache = hostCache;
    }

    @Override
    public String getType() {
        return "PONG";
    }
}
