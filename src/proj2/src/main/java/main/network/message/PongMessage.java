package main.network.message;

import main.network.neighbour.Host;

import java.util.List;

public class PongMessage extends Message {
    public int degree;
    public List<Host> storedHostCache;
    public List<String> storedTimelines;

    public PongMessage(int degree, List<Host> storedHostCache, List<String> storedTimelines) {
        this.degree = degree;
        this.storedHostCache = storedHostCache;
        this.storedTimelines = storedTimelines;
    }

    @Override
    public String getType() {
        return "PONG";
    }
}
