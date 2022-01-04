package main.network.neighbour;

import java.net.InetAddress;

public class Host {
    private InetAddress address;
    private String port;
    private int capacity;

    public Host(InetAddress address, String port, int capacity) {
        this.address = address;
        this.port = port;
        this.capacity = capacity;
    }
}
