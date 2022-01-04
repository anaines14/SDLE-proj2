package main.network.neighbour;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Neighbour extends Host{
    private final List<String> stored_timelines;

    public Neighbour(String username, InetAddress address, String port, int capacity, int degree) {
        super(username, address, port, capacity, degree);
        this.stored_timelines = new ArrayList<>();
    }

    public Neighbour (Host host) {
        super(host);
        this.stored_timelines = new ArrayList<>();
    }
}