package main.network.neighbour;


import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Neighbour extends Host implements Serializable {
    private final List<String> timelines;

    public Neighbour(String username, InetAddress address, String port, int capacity, List<String> timelines) {
        super(username, address, port, capacity);
        this.timelines = timelines;
    }

    public Neighbour (Host host) {
        super(host);
        this.timelines = new ArrayList<>();
    }

    private Integer getDegree() {
        return timelines.size();
    }

    public String toString() {
        return super.toString() + " Degree: " + getDegree();
    }
}