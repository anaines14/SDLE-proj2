package main.network;

import main.network.neighbour.Host;
import main.network.neighbour.Neighbour;
import main.timelines.Timeline;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

// Data class that serves like a Model in an MVC
public class PeerInfo {
    public final InetAddress address;
    public final String port;
    public final String username;
    public Map<String, Timeline> timelines;
    public final int capacity; // Quantity of messages that we can handle, arbitrary for us
    private Set<Neighbour> neighbours;
    public Set<Host> hostCache;

    public PeerInfo(InetAddress address, String port, String username, int capacity, Map<String, Timeline> timelines) {
        this.address = address;
        this.port = port;
        this.username = username;
        this.capacity = capacity;
        this.timelines = timelines;
        this.neighbours = new HashSet<>();
        this.hostCache = new HashSet<>();
    }

    public PeerInfo(InetAddress address, String port, String username, int capacity) {
        this(address, port, username, capacity, new HashMap<>());
    }

    public Set<Neighbour> getNeighbours() { return neighbours; }

    public Integer getDegree() {
        return neighbours.size();
    }

    /* Returns list of usernames that we have timelines to */
    public List<String> getStoredTimelines() {
        return timelines.keySet().stream().toList();
    }

    public boolean hasNeighbour(Neighbour neighbour) {
        return neighbours.contains(neighbour);
    }

    public void updateNeighbour(Neighbour updated) {
        neighbours.remove(updated);
        neighbours.add(updated);
    }

    public void addNeighbour(Neighbour neighbour) {
        neighbours.add(neighbour);
    }

    public static void main(String[] args) {
        try {
            PeerInfo peer1 = new PeerInfo(InetAddress.getByName("localhost"), "8000", "user1", 30);
            List<String> timelines1 = new ArrayList<>(Arrays.asList("u1", "u2", "u3"));
            List<String> timelines2 = new ArrayList<>(Arrays.asList("u1", "u2", "u3", "u4"));
            List<String> timelines3 = new ArrayList<>(Arrays.asList("u1", "u2", "u3", "u4", "u5"));
            Neighbour n1 = new Neighbour("u1", InetAddress.getByName("localhost"), "8000", 50, 1, timelines1);
            Neighbour n2 = new Neighbour("u2", InetAddress.getByName("localhost"), "8001", 50, 3, timelines2);
            Neighbour n3 = new Neighbour("u1", InetAddress.getByName("localhost"), "8000", 60, 4, timelines3);

            for (Neighbour n: peer1.getNeighbours())
                System.out.println(n.toString());
            System.out.println("---------------------");

            peer1.addNeighbour(n1);
            for (Neighbour n: peer1.getNeighbours())
                System.out.println(n.toString());
            System.out.println("---------------------");

            peer1.addNeighbour(n2);
            for (Neighbour n: peer1.getNeighbours())
                System.out.println(n.toString());
            System.out.println("---------------------");

            peer1.updateNeighbour(n3);
            for (Neighbour n: peer1.getNeighbours())
                System.out.println(n.toString());
            System.out.println("---------------------");

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }
}
