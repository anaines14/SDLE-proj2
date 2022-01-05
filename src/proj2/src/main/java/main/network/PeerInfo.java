package main.network;

import main.network.neighbour.Host;
import main.network.neighbour.Neighbour;
import main.timelines.Timeline;

import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

// Data class that serves like a Model in an MVC
public class PeerInfo {
    public final InetAddress address;
    public final String port;
    public final String username;
    public Map<String, Timeline> timelines;
    public final int capacity; // Quantity of messages that we can handle, arbitrary for us
    private Set<Neighbour> neighbours;
    private Set<Host> hostCache;

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

    // Neighbours

    public boolean hasNeighbour(Neighbour neighbour) {
        return neighbours.contains(neighbour);
    }

    public void replaceNeighbour(Neighbour oldNeigh, Neighbour newNeigh) {
        neighbours.remove(oldNeigh);
        neighbours.add(newNeigh);
    }

    public void updateNeighbour(Neighbour updated) {
        neighbours.remove(updated);
        neighbours.add(updated);
    }

    public void addNeighbour(Neighbour neighbour) {
        neighbours.add(neighbour);
        hostCache.add(neighbour); // Everytime we add a neighbour, we also add to the hostcache
    }

    public void removeNeighbour(Neighbour neighbour) {
        neighbours.remove(neighbour);
    }

    public Neighbour getWorstNeighbour(int hostCapacity) {
        // get neighbors with less capacity than host
        List<Neighbour> badNgbrs = neighbours.stream()
                .filter(n -> n.getCapacity() <= hostCapacity).toList();
        if (badNgbrs.isEmpty()) return null; // REJECT host if there are no worse neighbours

        // from neighbours with less capacity than host, get the one with max degree
        return badNgbrs.stream().max(Host::compareTo).get();
    }

    public Neighbour getBestNeighbour() { // With highest capacity
        return neighbours.stream().max(Host::compareTo).get();
    }

    public Set<Neighbour> getNeighboursWithTimeline(String timeline) {
         return neighbours.stream().filter(n -> n.hasTimeline(timeline)).collect(Collectors.toSet());
    }

    // HostCache

    public void addHost(Host host) {
        if (hostCache.contains(host))
            return;
        hostCache.add(host);
    }

    public void removeHost(Host host) {
        if (!hostCache.contains(host))
            return;
        hostCache.remove(host);
    }

    public Host getBestHostNotNeighbour() {
        // filter already neighbors
        Set<Host> notNeighbors = hostCache.stream()
                .filter(f -> !neighbours.contains(f))
                .collect(Collectors.toSet());

        Optional<Host> best_host = notNeighbors.stream().max(Host::compareTo);
        if(best_host.isEmpty()) return null;

        return best_host.get();
    }

    @Override
    public String toString() {
        return  username + " => " + address + ":" + port + " cap=" + capacity + " degree=" + neighbours.size();
    }
}
