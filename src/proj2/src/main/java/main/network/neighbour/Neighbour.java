package main.network.neighbour;


import main.network.PeerInfo;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class Neighbour extends Host implements Serializable{
    private final List<String> timelines;

    public Neighbour(String username, InetAddress address, String port, int capacity, int degree, List<String> timelines) {
        super(username, address, port, capacity, degree);
        this.timelines = timelines;
    }

    public Neighbour(String username, InetAddress address, String port, int capacity, int degree) {
        this(username, address, port, capacity, degree, new ArrayList<>());
    }

    public Neighbour(PeerInfo peerInfo) {
        super(peerInfo.username, peerInfo.address, peerInfo.port, peerInfo.capacity, peerInfo.getDegree());
        this.timelines = new ArrayList<>();
    }

    public Neighbour(Host host) {
        super(host);
        this.timelines = new ArrayList<>();
    }

    public List<String> getTimelines() {
        return timelines;
    }

    public String toString() {
        return super.toString() + " Degree: " + getDegree();
    }
}