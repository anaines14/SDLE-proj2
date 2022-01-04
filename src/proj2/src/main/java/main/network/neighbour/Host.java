package main.network.neighbour;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

// Data class
public class Host implements Serializable {
    private final InetAddress address;
    private final String port;
    private final int capacity;
    // needed to add as neighbor
    private final String username;

    public Host(String username, InetAddress address, String port, int capacity) {
        this.address = address;
        this.port = port;
        this.capacity = capacity;
        this.username = username;
    }

    public Host(Host host) {
        this(host.username, host.address, host.port, host.capacity);
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
    public String toString() {
        return "Username: " + username + " IP: " + address.getHostName() + ":" + port + " Cap: " + capacity;
    }

    public int getCapacity() {
        return capacity;
    }
}
