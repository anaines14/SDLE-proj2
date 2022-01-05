package main.network.neighbour;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

// Data class
public class Host implements Serializable, Comparable<Host> {
    private final InetAddress address;
    private final String port;
    private final int capacity; // Quantity of messages that we can handle, arbitrary for us
    // needed to add as neighbor
    private final String username;
    private int degree;

    public Host(String username, InetAddress address, String port, int capacity, int degree) {
        this.address = address;
        this.port = port;
        this.capacity = capacity;
        this.username = username;
        this.degree = degree;
    }

    public Host(Host host) {
        this(host.username, host.address, host.port, host.capacity, host.degree);
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getPort() {
        return port;
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

    @Override
    public String toString() {
        return "Username: " + username + " IP: " + address.getHostName() +
                ":" + port + " Cap: " + capacity + " Deg: " + degree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Host)) return false;
        Host host = (Host) o;
        return Objects.equals(address, host.address) && Objects.equals(port, host.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }

    @Override
    public int compareTo(Host host) {
        if (capacity == host.capacity)
            return Integer.compare(host.degree, degree);
        return Integer.compare(host.capacity, capacity);
    }
}
