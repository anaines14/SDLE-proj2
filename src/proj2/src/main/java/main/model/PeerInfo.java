package main.model;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import main.gui.Observer;
import main.model.neighbour.Host;
import main.model.neighbour.Neighbour;
import main.model.timelines.TimelineInfo;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static main.Peer.SP_MIN;

// Data class that serves like a Model in an MVC
public class PeerInfo {
    private Host me;
    private TimelineInfo timelineInfo;
    private Set<Neighbour> neighbours;
    private Set<Host> hostCache;
    private Observer observer;
    private BloomFilter<String> timelinesFilter;
    private final int max_nbrs;

    public PeerInfo(String username, InetAddress address, int capacity, TimelineInfo timelineInfo) {
        this.max_nbrs = (int) Math.floor(capacity * 0.3);
        this.me = new Host(username, address, "-1", capacity, 0, max_nbrs);
        this.timelineInfo = timelineInfo;
        this.neighbours = ConcurrentHashMap.newKeySet();
        this.hostCache = ConcurrentHashMap.newKeySet();
        this.timelinesFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 100);
        this.timelinesFilter.put(username);
    }

    public PeerInfo(InetAddress address, String username, int capacity) {
        this(username, address, capacity, new TimelineInfo(username));
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

        // if we arent a super peer
        if (!this.isSuperPeer()) {
            for (Neighbour n: neighbours) {
                // if we already are connected to a super peer
                if (this.isSuperPeer(n)) {
                    // remove super peers from set => Clusters
                    notNeighbors = notNeighbors.stream()
                            .filter(f -> !this.isSuperPeer(f))
                            .collect(Collectors.toSet());
                    break;
                }
            }
        }

        Optional<Host> best_host = notNeighbors.stream().min(Host::compareTo);
        if(best_host.isEmpty()) return null;
        return best_host.get();
    }

    // observers
    public void subscribe(Observer o) {
        this.observer = o;
        this.observer.newNodeUpdate(this.getUsername(), this.getPort(), this.getCapacity());
    }

    // bloom filters

    public boolean isSuperPeer() {
        return this.getMaxNbrs() >= SP_MIN;
    }

    public boolean isSuperPeer(Host host) {
        return host.getMaxNbrs() >= SP_MIN;
    }

    public void resetFilter() {
        this.timelinesFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 100);
        this.timelinesFilter.put(this.getUsername());
    }

    public void mergeFilter(Neighbour neighbour) {
        // merge filters only if neighbour isn't a super peer
        System.out.println(neighbour.getUsername() + " tem " + neighbour.getMaxNbrs());
        if (!this.isSuperPeer(neighbour)) {
            this.timelinesFilter.putAll(neighbour.getTimelines());
            System.out.println("PUt");
        }
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

    public Host getHost() {
        return this.me;
    }

    public int getCapacity() {
        return this.me.getCapacity();
    }

    public boolean areNeighboursFull() {
        return this.neighbours.size() >= max_nbrs;
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

    public int getMaxNbrs() {
        return max_nbrs;
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

    public void setPort(String port) {
        this.me.setPort(port);
    }

}
