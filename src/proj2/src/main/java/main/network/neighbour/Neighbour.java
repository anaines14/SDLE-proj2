package main.network.neighbour;

import main.timelines.Timeline;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Neighbour extends Host{
    private final List<Timeline> timelines;

    public Neighbour(String username, InetAddress address, String port, int capacity, List<Timeline> timelines) {
        super(username, address, port, capacity);
        this.timelines = timelines;
    }

    public Neighbour (Host host) {
        super(host);
        this.timelines = new ArrayList<>();
    }
}