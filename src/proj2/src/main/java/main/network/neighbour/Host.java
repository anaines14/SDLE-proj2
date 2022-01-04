package main.network.neighbour;

import java.net.InetAddress;

// Data class
public class Host {
    public InetAddress address;
    public String port;
    public int capacity;

    public Host(InetAddress address, String port, int capacity) {
        this.address = address;
        this.port = port;
        this.capacity = capacity;
    }
}
