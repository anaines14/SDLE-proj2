package main.network.message;

import main.network.PeerInfo;
import main.network.neighbour.Neighbour;

public class PongMessage extends MessageResponse {
    public Neighbour sender;

    public PongMessage(PeerInfo info, Neighbour sender) {
        super(info);
        this.sender = sender;
    }

    @Override
    public String getType() {
        return "PONG";
    }
}
