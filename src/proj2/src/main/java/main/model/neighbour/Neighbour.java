package main.model.neighbour;


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

    public Neighbour(Host host) {
        super(host);
        this.timelines = new ArrayList<>();
    }

    public boolean hasTimeline(String username) {
        return timelines.contains(username);
    }

    public List<String> getTimelines() {
        return timelines;
    }

    public String toString() {
        return super.toString() + " Degree: " + getDegree();
    }
}