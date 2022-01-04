package main.network.message;

import main.network.neighbour.Neighbour;

public class PongMessage extends Message {
    Neighbour sender;

    public PongMessage(Neighbour sender) {
        this.sender = sender;
    }

    @Override
    public String getType() {
        return "PONG";
    }
}
