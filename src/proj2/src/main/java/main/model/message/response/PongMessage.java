package main.model.message.response;

import main.model.message.request.MessageRequest;
import main.model.neighbour.Host;
import main.model.neighbour.Neighbour;

import java.util.Set;
import java.util.UUID;

public class PongMessage extends MessageResponse {
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
