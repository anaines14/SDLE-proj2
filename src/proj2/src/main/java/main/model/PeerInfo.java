package main.model;

import main.model.neighbour.Host;
import main.model.neighbour.Neighbour;
import main.model.timelines.Timeline;

import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

// Data class that serves like a Model in an MVC
public class PeerInfo {
    private Host me;
    public Map<String, Timeline> timelines;
    private Set<Neighbour> neighbours;
    private Set<Host> hostCache;

    public PeerInfo(String username, InetAddress address, String port, int capacity, Map<String, Timeline> timelines) {
        this.me = new Host(username, address, port, capacity, 0);
        this.timelines = timelines;
        this.neighbours = new HashSet<>();
        this.hostCache = new HashSet<>();
    }

    public PeerInfo(InetAddress address, String port, String username, int capacity) {
        this(username, address, port, capacity, new HashMap<>());
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

    public void updateHostCache(Set<Host> hostCache) {
        Set<Host> filterOurselvesOut = hostCache.stream().filter(
                host -> !host.equals(this.getHost())
        ).collect(Collectors.toSet());

        this.hostCache.addAll(filterOurselvesOut);
    }

    public void addNeighbour(Neighbour neighbour) {
        if (neighbour.equals(this.me)) // We can't add ourselves as a neighbour
            return;

        System.out.println(this.me.getUsername() + " ADDED " + neighbour.getUsername());
        neighbours.add(neighbour);
        this.me.setDegree(neighbours.size());
        hostCache.add(neighbour); // Everytime we add a neighbour, we also add to the hostcache
    }

    public void removeNeighbour(Neighbour neighbour) {
        if (!neighbours.contains(neighbour))
            return;

        neighbours.remove(neighbour);
        this.me.setDegree(neighbours.size());
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

    public Set<Neighbour> getNeighboursWithTimeline(String username) {
        for (Neighbour n: neighbours)
            System.out.println(n.getUsername());
        return neighbours.stream().filter(n -> n.hasTimeline(username)).collect(Collectors.toSet());
    }

    // HostCache

    public void addHost(Host host) {

        if (hostCache.contains(host) || this.me.equals(host))
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

    public String getUsername() {
        return this.me.getUsername();
    }

    public InetAddress getAddress() {
        return this.me.getAddress();
    }

    public String getPort() {
        return this.me.getPort();
    }

    public Host getHost() {
        return this.me;
    }

    public Set<Neighbour> getNeighbours() { return neighbours; }

    public Set<Host> getHostCache() {
        return this.hostCache;
    }

    public Integer getDegree() {
        return me.getDegree();
    }

    /* Returns list of usernames that we have timelines to */
    public List<String> getStoredTimelines() {
        return timelines.keySet().stream().toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeerInfo peerInfo = (PeerInfo) o;
        return Objects.equals(me, peerInfo.me);
    }
}
