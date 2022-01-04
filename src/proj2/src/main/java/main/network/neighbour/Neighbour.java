package main.network.neighbour;

import main.timelines.Timeline;

import java.net.InetAddress;
import java.util.List;

public class Neighbour {
    private String usrname;
    private InetAddress address;
    private String port;
    private int capacity;
    private List<Timeline> timelines;

    public Neighbour(String usrname, InetAddress address, String port, int capacity, List<Timeline> timelines) {
        this.usrname = usrname;
        this.address = address;
        this.port = port;
        this.capacity = capacity;
        this.timelines = timelines;
    }
}