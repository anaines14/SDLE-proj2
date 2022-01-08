package main.model.message.request;

import main.model.neighbour.Host;
import main.model.neighbour.Neighbour;

import java.util.Set;
import java.util.UUID;

public class PongMessage extends MessageRequest {
    public Neighbour sender;
    public Set<Host> hostCache;
    public boolean isNeighbour;

    public PongMessage(Neighbour sender, Set<Host> hostCache, UUID id, boolean isNeighbour) {
        super(id);
        this.sender = sender;
        this.hostCache = hostCache;
        this.isNeighbour = isNeighbour;
    }

    @Override
    public String getType() {
        return "PONG";
    }
}
