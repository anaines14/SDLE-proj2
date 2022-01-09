package main.model.neighbour;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static main.Peer.MAX_SUBS;

// Data class
public class Host implements Serializable, Comparable<Host> {
    private final InetAddress address;
    private final String port;
    private final String publisherPort;
    private final int capacity; // Quantity of messages that we can handle, arbitrary for us
    private int maxSubCapacity; // capacity at a given time (starts at a given number and decreases with new subs)
    // needed to add as neighbor
    private final String username;
    private int degree;

    public Host(String username, InetAddress address, String port, String publishPort,
                int capacity, int degree, int subCapacity) {
        this.address = address;
        this.port = port;
        this.publisherPort = publishPort;
        this.capacity = capacity;
        this.username = username;
        this.degree = degree;
        this.maxSubCapacity = subCapacity;
    }

    public Host(String username, InetAddress address, String port, String publishPort,
                int capacity, int degree) {
        this(username, address, port, publishPort, capacity, degree, MAX_SUBS);
    }

    public Host(Host host) {
        this(host.username, host.address, host.port, host.publisherPort, host.capacity, host.degree, host.maxSubCapacity);
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getPort() {
        return port;
    }

    public String getPublishPort() {
        return publisherPort;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getDegree() {
        return degree;
    }

    public String getUsername() { return username; }

    public void setDegree(int size) {
        this.degree = size;
    }

    public int getMaxSubCapacity() { return maxSubCapacity; }

    // for testing
    public void setMaxSubCapacity(int newCap) { this.maxSubCapacity = newCap; }

    @Override
    public String toString() {
        return "Username: " + username + " IP: " + address.getHostName() +
                ":" + port + " Cap: " + capacity + " Deg: " + degree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Host host)) return false;
        return Objects.equals(address, host.address) && Objects.equals(port, host.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }

    @Override
    public int compareTo(Host host) {
        return Integer.compare(degree, host.degree);
    }

}
