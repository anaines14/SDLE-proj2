package main.network;

import main.network.neighbour.Host;
import main.network.neighbour.Neighbour;
import main.timelines.Timeline;

import java.net.InetAddress;
import java.util.*;

// Data class that serves like a Model in an MVC
public class PeerInfo {
    public final InetAddress address;
    public final String port;
    public final String username;
    public Map<String, Timeline> timelines;
    public final int capacity; // Quantity of messages that we can handle, arbitrary for us
    public List<Neighbour> neighbours;
    public Set<Host> hostCache;

    public PeerInfo(InetAddress address, String port, String username, int capacity, Map<String, Timeline> timelines) {
        this.address = address;
        this.port = port;
        this.username = username;
        this.capacity = capacity;
        this.timelines = timelines;
        this.neighbours = new ArrayList<>();
        this.hostCache = new HashSet<>();
    }

    public PeerInfo(InetAddress address, String port, String username, int capacity) {
        this(address, port, username, capacity, new HashMap<>());
    }

    public Integer getDegree() {
        return neighbours.size();
    }

    /* Returns list of usernames that we have timelines to */
    public List<String> getStoredTimelines() {
        return timelines.keySet().stream().toList();
    }
}
