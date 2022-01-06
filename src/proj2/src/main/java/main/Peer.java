package main;

import main.model.PeerInfo;
import main.controller.network.Broker;
import main.controller.message.MessageSender;
import main.model.message.request.MessageRequest;
import main.model.message.request.PingMessage;
import main.model.message.request.QueryMessage;
import main.model.message.response.MessageResponse;
import main.model.message.response.PongMessage;
import main.model.neighbour.Host;
import main.model.neighbour.Neighbour;
import main.model.timelines.TimelineInfo;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.Collections.shuffle;

public class Peer implements Serializable {
    public static final int PINGNEIGH_DELAY = 1000;
    public static final int ADDNEIGH_DELAY = 1000;
    public static final int MAX_NGBRS = 2;
    public static final int MAX_RETRY = 3;
    public static final int RCV_TIMEOUT = 1000;
    public static final int MAX_RANDOM_NEIGH = 2;

    // Model/Data members
    private final PeerInfo peerInfo;
    private final ZContext context;

    // Network members
    private final Broker broker;
    private final MessageSender sender;

    // Hooks
    private ScheduledFuture<?> pingNeighFuture;
    private ScheduledFuture<?> addNeighFuture;

    public Peer(String username, InetAddress address, int capacity) {
        this.context = new ZContext();
        this.peerInfo = new PeerInfo(address, username, capacity);
        this.sender = new MessageSender(peerInfo, MAX_RETRY, RCV_TIMEOUT, context);
        this.broker = new Broker(context, sender, peerInfo);
    }

    public void join(Neighbour neighbour) {
        peerInfo.addNeighbour(neighbour);
    }

    public void join(Peer peer) {
        peerInfo.addNeighbour(new Neighbour(peer.getPeerInfo().getHost()));
    }

    public void printTimelines() {
        TimelineInfo timelineInfo = peerInfo.getTimelineInfo();
        timelineInfo.printTimelines();
    }

    public void updatePost(int postId, String newContent) {
        TimelineInfo timelineInfo = peerInfo.getTimelineInfo();
        timelineInfo.updatePost(peerInfo.getUsername(), postId, newContent);
    }

    public void addPost(String newContent) {
        TimelineInfo timelineInfo = peerInfo.getTimelineInfo();
        timelineInfo.addPost(peerInfo.getUsername(), newContent);
    }

    public void deletePost(int postId) {
        TimelineInfo timelineInfo = peerInfo.getTimelineInfo();
        timelineInfo.deletePost(peerInfo.getUsername(), postId);
    }

    public void execute(ScheduledThreadPoolExecutor scheduler) {
        this.broker.execute();
        pingNeighFuture = scheduler.scheduleWithFixedDelay(this::pingNeighbours,
                0, PINGNEIGH_DELAY, TimeUnit.MILLISECONDS);
        addNeighFuture = scheduler.scheduleWithFixedDelay(this::addNeighbour,
                0, ADDNEIGH_DELAY, TimeUnit.MILLISECONDS);
    }

    public void cancelHooks() {
        if (pingNeighFuture != null) pingNeighFuture.cancel(false);
        if (addNeighFuture != null) addNeighFuture.cancel(false);
    }

    public void stop() {
        this.broker.stop();
        this.cancelHooks();
        this.context.close();
    }

    public void queryNeighbours(String username) {
        // check if neighbours have the username's timeline
        // TODO: BLOOM FILTERS
        // TODO Tamos a dar flooding atm, dps temos que usar searches
        // System.out.println("got neighbours with timelines: " + neighbours.size());
        List<Neighbour> neighbours = peerInfo.getNeighbours().stream().toList();

        // Get random N neighbours to send
        int[] randomNeighbours = IntStream.range(0, neighbours.size()).toArray();

        int i=0;
        while (i < randomNeighbours.length && i < MAX_RANDOM_NEIGH) {
            Neighbour n = neighbours.get(i);
            MessageRequest request = new QueryMessage(username, this.peerInfo);
            this.sender.sendRequestNTimes(request, n.getPort());
            ++i;
        }
    }

    public void pingNeighbours() {
        for (Neighbour neighbour: peerInfo.getNeighbours()) { // TODO multithread this, probably with scheduler
            PingMessage pingMessage = new PingMessage(peerInfo.getHost());
            MessageResponse response = this.sender.sendRequestNTimes(pingMessage, neighbour.getPort());

            if (response == null) { // Went offline after n tries
                System.out.println(peerInfo.getUsername() + " REMOVED " + neighbour.getUsername());
                peerInfo.removeNeighbour(neighbour);
                peerInfo.removeHost(neighbour);
                continue;
            }

            PongMessage pong = (PongMessage) response;
            Neighbour updatedNeighbour = pong.sender;
            if (peerInfo.hasNeighbour(updatedNeighbour)) {
                Set<Host> hostCache = pong.hostCache;
                System.out.println(peerInfo.getUsername() + " UPDATED " + neighbour.getUsername());
                peerInfo.updateNeighbour(updatedNeighbour);
                peerInfo.updateHostCache(hostCache);
            }
        }
    }

    public void addNeighbour()  {
        // get higher capacity host not neighbour
        Host host = peerInfo.getBestHostNotNeighbour();
        if (host == null) return;
        // ACCEPT host if limit not reached
        if (peerInfo.getNeighbours().size() < MAX_NGBRS) {
            peerInfo.addNeighbour(new Neighbour(host));
            return;
        }

        // from neighbours with less or equal capacity than host, get the one with max degree
        Neighbour worstNgbr = peerInfo.getWorstNeighbour(host.getCapacity());
        if (worstNgbr == null) return; // REJECT host if there are no worse neighbours

        // get highest capacity node
        Neighbour bestNgbr = peerInfo.getBestNeighbour();

        // host has higher capacity than every neighbour
        boolean hostHigherCap = host.getCapacity() > worstNgbr.getCapacity(),
                // host has lower degree than worst neighbour (less busy)
                hostLowerDegree = host.getDegree() < bestNgbr.getDegree();

        if (hostHigherCap || hostLowerDegree)
            peerInfo.replaceNeighbour(worstNgbr, new Neighbour(host));
        // REJECT host
    }

    public PeerInfo getPeerInfo() {
        return this.peerInfo;
    }

    public Broker getBroker() {
        return broker;
    }

    @Override
    public String toString() {
        return peerInfo.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peer peer = (Peer) o;
        return Objects.equals(peerInfo, peer.peerInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerInfo);
    }
}