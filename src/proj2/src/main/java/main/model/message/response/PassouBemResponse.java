package main.model.message.response;

import main.model.message.request.MessageRequest;
import main.model.neighbour.Host;

import java.util.Set;
import java.util.UUID;

public class PassouBemResponse extends MessageResponse {
    private Set<Host> hostCache;
    private boolean accepted;

    public PassouBemResponse(UUID id, Set<Host> hostCache, boolean accepted) {
        super(id);
        this.hostCache = hostCache;
        this.accepted = accepted;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public Set<Host> getHostCache() {
        return hostCache;
    }

    @Override
    public String getType() {
        return "PASSOU_BEM_RESPONSE";
    }

    public String toString() {
        return super.toString() + " " + accepted;
    }
}
