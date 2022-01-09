package main.model.neighbour;

import main.model.SocketInfo;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static main.Peer.MAX_SUBS;

import static main.Peer.MAX_SUBS;

// Data class
public class Host implements Serializable, Comparable<Host> {
    private final String username;
    private final InetAddress address;
    private final int capacity; // Quantity of messages that we can handle, arbitrary for us
    private int degree; // needed to add as neighbor
    private int maxSubCapacity; // capacity at a given time (starts at a given number and decreases with new subs)
    private String port;
    private String publisherPort;

    public Host(String username, InetAddress address, int capacity, int degree, int maxSubCapacity, String port, String publisherPort) {
        this.username = username;
        this.address = address;
        this.capacity = capacity;
        this.degree = degree;
        this.maxSubCapacity = maxSubCapacity;
        this.port = port;
        this.publisherPort = publisherPort;
    }

    public Host(String username, InetAddress address, String port, String publishPort,
                int capacity, int degree) {
        this(username, address, capacity, degree, MAX_SUBS, port, publishPort);
    }

    public Host(String username, InetAddress address, int capacity, int degree, String port, String publisherPort) {
        this(username, address, capacity, degree, MAX_SUBS, port, publisherPort);
    }

    public Host(Host host) {
        this(host.username, host.address, host.capacity, host.degree, host.maxSubCapacity, host.port, host.publisherPort);
    }

    public Host(String username, InetAddress address, int capacity, int degree, int subCapacity, SocketInfo socketInfo) {
        this(username, address, capacity, degree, subCapacity, socketInfo.getFrontendPort(), socketInfo.getPublisherPort());
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String frontendPort) {
        this.port = frontendPort;
    }

    public String getPublishPort() {
        return publisherPort;
    }
    public void setPublishPort(String publisherPort) {
        this.publisherPort = publisherPort;
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
