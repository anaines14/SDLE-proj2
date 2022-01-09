package main.model;

import main.gui.Observer;
import main.model.neighbour.Host;
import main.model.neighbour.Neighbour;
import main.model.timelines.TimelineInfo;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static main.Peer.MAX_NGBRS;
import static main.Peer.MAX_SUBS;

// Data class that serves like a Model in an MVC
public class PeerInfo {
    private Host me;
    private TimelineInfo timelineInfo;
    private Set<Neighbour> neighbours;
    private Set<String> subscriptions; // peers subscribed by me
    private final Set<String> subscribers;
    private Set<Host> hostCache;
    private Observer observer;

    public PeerInfo(String username, InetAddress address, int capacity,
                    String port, String publishPort, TimelineInfo timelineInfo) {
        this.me = new Host(username, address, port, publishPort, capacity, 0, MAX_SUBS);
        this.timelineInfo = timelineInfo;
        this.neighbours = ConcurrentHashMap.newKeySet();
        this.hostCache = ConcurrentHashMap.newKeySet();
        this.subscriptions = ConcurrentHashMap.newKeySet();
        this.subscribers = ConcurrentHashMap.newKeySet();
    }

    public PeerInfo(String username, InetAddress address, int capacity, String port, String publishPort) {
        this(username, address, capacity, port, publishPort, new TimelineInfo(username));
    }

    // Neighbours

    public boolean hasNeighbour(Neighbour neighbour) {
        return neighbours.contains(neighbour);
    }

    public void replaceNeighbour(Neighbour oldNeigh, Neighbour newNeigh) {
        this.removeNeighbour(oldNeigh);
        this.addNeighbour(newNeigh);
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
        // System.out.println(this.getUsername() + " ADDED " + neighbour.getUsername());

        // System.out.println(this.me.getUsername() + " ADDED " + neighbour.getUsername());
        neighbours.add(neighbour);
        this.me.setDegree(neighbours.size());
        hostCache.add(neighbour); // Everytime we add a neighbour, we also add to the hostcache

        if (this.observer != null)
            this.observer.newEdgeUpdate(this.getPort(), neighbour.getPort());
    }

    public void removeNeighbour(Neighbour neighbour) {
        if (!neighbours.contains(neighbour))
            return;

        // System.out.println(this.getUsername() + " REMOVED " + neighbour.getUsername());
        neighbours.remove(neighbour);
        this.me.setDegree(neighbours.size());

        if (this.observer != null)
            this.observer.removeEdgeUpdate(this.getUsername(), neighbour.getUsername());
    }

    public Set<Neighbour> getNeighboursWithTimeline(String username) {
        for (Neighbour n: neighbours)
            System.out.println(n.getUsername());
        return neighbours.stream().filter(n -> n.hasTimeline(username)).collect(Collectors.toSet());
    }

    public void addSubscription(String username) { this.subscriptions.add(username); }
    public void removeSubscription(String username) { this.subscriptions.remove(username); }
    public boolean hasSubscription(String username) { return this.subscriptions.contains(username); }

    public void addSubscriber(String username) { this.subscribers.add(username); }
    public void removeSubscriber(String username) { this.subscribers.remove(username); }
    public boolean canAcceptSub() { return this.subscribers.size() < this.me.getMaxSubCapacity(); }
    public boolean hasSubscriber(String username) {
        return this.subscribers.contains(username);
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


        Optional<Host> best_host = notNeighbors.stream().min(Host::compareTo);
        if(best_host.isEmpty()) return null;
        return best_host.get();
    }

    // observers
    public void subscribe(Observer o) {
        this.observer = o;
        this.observer.newNodeUpdate(this.getUsername(), this.getPort(), this.getCapacity());
    }

    // getters

    public String getUsername() {
        return this.me.getUsername();
    }

    public InetAddress getAddress() {
        return this.me.getAddress();
    }

    public String getPort() {
        return this.me.getPort();
    }

    public String getPublishPort() {
        return this.me.getPublishPort();
    }

    public Host getHost() {
        return this.me;
    }

    public int getCapacity() {
        return this.me.getCapacity();
    }

    public boolean areNeighboursFull() {
        return this.neighbours.size() >= MAX_NGBRS;
    }

    public Set<Neighbour> getNeighbours() { return neighbours; }

    public Set<Host> getHostCache() {
        return this.hostCache;
    }

    public Integer getDegree() {
        return me.getDegree();
    }

    public TimelineInfo getTimelineInfo() {
        return timelineInfo;
    }

    // Returns worst neighbour if we need to replace Neighbour
    // Returns null if we can't replace candidate
    public Neighbour acceptNeighbour(Host candidate) {
        // from neighbours with less or equal capacity than host, get the one with max degree


        // get neighbors with less capacity than val
        List<Neighbour> worstNgbrs = neighbours.stream()
                .filter(n -> n.getCapacity() <= candidate.getCapacity()).toList();
        if (worstNgbrs.isEmpty()) return null;

        Neighbour highestDegNeigh = worstNgbrs.stream().max(Host::compareTo).get();

        // get highest capacity neihbour
        Neighbour highestCapNgbr = neighbours.stream().max(Comparator.comparingInt(Host::getCapacity)).get();

        // candidate has higher capacity than every neighbour
        boolean candidateHigherCap = candidate.getCapacity() > highestCapNgbr.getCapacity();
        // candidate has fewer neighbours
        int hysteresis = 0;
        boolean candidateFewerNeighs = candidate.getDegree() + hysteresis < highestDegNeigh.getDegree();

        if (candidateHigherCap || candidateFewerNeighs)
            return highestCapNgbr;
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeerInfo peerInfo = (PeerInfo) o;
        return Objects.equals(me, peerInfo.me);
    }
}
