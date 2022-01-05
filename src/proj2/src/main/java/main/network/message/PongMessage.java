package main.network.message;

import main.network.neighbour.Host;
import main.network.neighbour.Neighbour;

import java.util.Set;

public class PongMessage extends MessageResponse {
    public Neighbour sender;
    public Set<Host> hostCache;


    public PongMessage(Neighbour sender, Set<Host> hostCache) {
        super(sender);
        this.sender = sender;
        this.hostCache = hostCache;
    }

    @Override
    public String getType() {
        return "PONG";
    }
}
