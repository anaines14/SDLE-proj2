package main.model;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import main.gui.Observer;
import main.model.neighbour.Host;
import main.model.neighbour.Neighbour;
import main.model.timelines.Post;
import main.model.timelines.TimelineInfo;

import java.net.InetAddress;
import java.security.PrivateKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static main.Peer.SP_MIN;
import static main.Peer.MAX_SUBS;

// Data class that serves like a Model in an MVC
public class PeerInfo {
    private Host me;
    private TimelineInfo timelineInfo;
    private Set<Neighbour> neighbours;
    private Set<String> subscriptions; // peers subscribed by me
    private final Set<String> subscribers;
    private final Map<String, Set<String>> redirections; // peers that are relaying on me to redirect info from a username
    private Set<Host> hostCache;
    private Observer observer;
    private BloomFilter<String> timelinesFilter;
    private final int max_nbrs;
    private PrivateKey privateKey;
    private boolean printToConsole;

    public PeerInfo(String username, InetAddress address, int capacity,
                    TimelineInfo timelineInfo, SocketInfo socketInfo) {
        this.max_nbrs = (int) Math.ceil(capacity * 0.3);
        this.me = new Host(username, address, capacity, 0, max_nbrs, MAX_SUBS, socketInfo);
        this.timelineInfo = timelineInfo;
        this.neighbours = ConcurrentHashMap.newKeySet();
        this.hostCache = ConcurrentHashMap.newKeySet();
        this.subscriptions = ConcurrentHashMap.newKeySet();
        this.redirections = new ConcurrentHashMap<>();
        this.subscribers = ConcurrentHashMap.newKeySet();
        this.timelinesFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 100);
        this.timelinesFilter.put(username);
        this.printToConsole = false;
    }

    public PeerInfo(String username, InetAddress address, int capacity, SocketInfo socketInfo) {
        this(username, address, capacity, new TimelineInfo(username), socketInfo);
    }

    // prints all timelines stored in order
    public void showFeed() {
        this.timelineInfo.showFeed();
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

        // System.out.println(this.me.getUsername() + " ADDED " + neighbour.getUsername());
        neighbours.add(neighbour);
        this.me.setDegree(neighbours.size());
        hostCache.add(neighbour); // Everytime we add a neighbour, we also add to the hostcache
    }

    public void removeNeighbour(Neighbour neighbour) {
        if (!neighbours.contains(neighbour))
            return;

        // System.out.println(this.getUsername() + " REMOVED " + neighbour.getUsername());
        neighbours.remove(neighbour);
        this.me.setDegree(neighbours.size());

        if (this.observer != null)
            this.observer.removeEdgeUpdate(this.getPort(), neighbour.getPort());
    }

    public Set<Neighbour> getNeighboursWithTimeline(String username) {
        return neighbours.stream().filter(n -> n.hasTimeline(username)).collect(Collectors.toSet());
    }

    // Subscriptions

    public void addSubscription(String username) { this.subscriptions.add(username); }
    public void removeSubscription(String username) { this.subscriptions.remove(username); }
    public boolean hasSubscription(String username) { return this.subscriptions.contains(username); }
    public void addPostOfSubscription(Post post) {
        this.timelineInfo.addPost(post.getUsername(), post.getContent());
    }

    public void addSubscriber(String port) { this.subscribers.add(port); }
    public void removeSubscriber(String username) { this.subscribers.remove(username); }
    public boolean canAcceptSub() { return this.subscribers.size() + this.redirections.size() < this.me.getMaxSubCapacity(); }
    public boolean hasSubscriber(String port) {
        return this.subscribers.contains(port);
    }
    public void addRedirect(String username, String port) {
        this.redirections.putIfAbsent(username, new HashSet<>());
        this.redirections.get(username).add(port);
    }


    // Subscribe to sout
    public void addContentListening() {
        this.printToConsole = true;
    }

    public void stopContentListening() {
        this.printToConsole = false;
    }

    public boolean hasContentListener() {
        return printToConsole;
    }

    // observers
    public void subscribe(Observer o) {
        this.observer = o;
        this.observer.newNodeUpdate(this.getUsername(), this.getPort(), this.getCapacity());
    }

    public void notifyNewNeighbour(Host host) {
        if (this.observer != null)
            this.observer.newEdgeUpdate(this.getPort(), host.getPort());
    }

    public void notifyNewSub(String port) {
        if (this.observer != null)
            this.observer.newSubUpdate(port, this.getPort());
    }

    public void notifyNewPost() {
        if (this.observer != null)
            this.observer.newPostUpdate(this.getPort(), this.subscribers);
    }

    public void notifyNewPost(String usernameRedirect) {
        if (this.observer != null) {
            this.observer.newPostUpdate(this.getPort(), this.redirections.get(usernameRedirect));
        }
    }

    public void notifyStop() {
        if (this.observer != null)
            this.observer.stopUpdate(this.getPort());
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
        int hysteresis = 3;
        boolean candidateFewerNeighs = candidate.getDegree() + hysteresis < highestDegNeigh.getDegree();

        if (candidateHigherCap || candidateFewerNeighs)
            return highestCapNgbr;
        return null;
    }

    // bloom filters

    public boolean isSuperPeer() {
        return this.isSuperPeer(me);
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
        if (!this.isSuperPeer(neighbour)) {
            this.timelinesFilter.putAll(neighbour.getTimelines());
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

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void logout() {
        privateKey = null;
    }

    public boolean isAuth() {
        return privateKey != null;
    }

    public BloomFilter<String> getTimelinesFilter() { return timelinesFilter; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeerInfo peerInfo = (PeerInfo) o;
        return Objects.equals(me, peerInfo.me);
    }
}
