package main.network;

import main.network.executor.MultipleNodeExecutor;
import main.network.message.*;
import main.network.neighbour.Host;
import main.network.neighbour.Neighbour;
import main.timelines.Timeline;
import main.timelines.TimelineInfo;
import org.zeromq.ZContext;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Peer implements Serializable {
    public static final int PINGNEIGH_DELAY = 1000;
    public static final int ADDNEIGH_DELAY = 1000;
    public static final int MAX_NGBRS = 3;
    public static final int RCV_TIMEOUT = 1000;

    // Model/Data members
    private final PeerInfo peerInfo;
    private final TimelineInfo timelineInfo;
    private final ZContext context;

    // Network members
    private final Broker broker;
    private final MessageSender sender;

    // Hooks
    private ScheduledFuture<?> pingNeigFuture;
    private ScheduledFuture<?> addNeighFuture;

    public Peer(String username, InetAddress address, String port, int capacity, ScheduledThreadPoolExecutor scheduler) {
        this.context = new ZContext();
        this.peerInfo = new PeerInfo(address, port, username, capacity);
        this.timelineInfo = new TimelineInfo(username);
        this.sender = new MessageSender(peerInfo, context);
        this.broker = new Broker(context, sender, peerInfo);
    }

    public Peer(String username, InetAddress address, String port, int capacity) {
        this(username, address, port, capacity, new ScheduledThreadPoolExecutor(MultipleNodeExecutor.POOL_SIZE));
    }

    public Peer(PeerInfo peerInfo) {
        this(peerInfo.username, peerInfo.address, peerInfo.port, peerInfo.capacity);
    }

    public void join(InetAddress address, String port) {}

    public Neighbour ping(Neighbour neighbour) {
        Neighbour responder = null;
        int i = 0;
        boolean done = false;
        while (i < 3 && !done) {
            MessageResponse response = this.sender.
                    sendRequest(new PingMessage(peerInfo), neighbour.getPort(), RCV_TIMEOUT);

            if (response != null && Objects.equals(response.getType(), "PONG")) {
                responder = ((PongMessage) response).sender;
                done = true;
            }

            ++i;
        }

        return responder;
    }

    public void queryNeighbour(String wantedTimeline, Neighbour neighbour) {
        this.sender.sendRequest(new QueryMessage(wantedTimeline, peerInfo), neighbour.getPort());

    }

    public void printTimelines() {
        this.timelineInfo.printTimelines();
    }

    public void updatePost(int postId, String newContent) {
        this.timelineInfo.updatePost(peerInfo.username, postId, newContent);
    }

    public void addPost(String newContent) {
        this.timelineInfo.addPost(peerInfo.username, newContent);
    }

    public void deletePost(int postId) {
        this.timelineInfo.deletePost(peerInfo.username, postId);
    }

    public void execute(ScheduledThreadPoolExecutor scheduler) {
        this.broker.execute();
        pingNeigFuture = scheduler.scheduleWithFixedDelay(this::pingNeighbours,
                0, PINGNEIGH_DELAY, TimeUnit.MILLISECONDS);
        addNeighFuture = scheduler.scheduleWithFixedDelay(this::addNeighbour,
                0, ADDNEIGH_DELAY, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        this.broker.stop();
        if (pingNeigFuture != null) pingNeigFuture.cancel(false);
        if (addNeighFuture != null) addNeighFuture.cancel(false);

        this.context.close();
    }

    public void queryNeighbours(String timeline) {
        // check if neighbours have the timeline
        // TODO: BLOOM FILTERS
        Set<Neighbour> neighbours = this.peerInfo.getNeighboursWithTimeline(timeline);
        System.out.println("got neighbours with timelibnes: " + neighbours.size());

        // query neighbours with timelines
        for(Neighbour n: neighbours) {
            queryNeighbour(timeline, n);
            System.out.println("query neighbour");
        }

    }

    public void pingNeighbours() {
        for (Neighbour neighbour: peerInfo.getNeighbours()) { // TODO multithread this, probably with scheduler
            Neighbour updatedNeighbour = ping(neighbour);

            if (updatedNeighbour == null) { // Went offline after n tries
                peerInfo.removeNeighbour(neighbour);
                peerInfo.removeHost(neighbour);
            }

            if (peerInfo.hasNeighbour(updatedNeighbour))
                peerInfo.updateNeighbour(updatedNeighbour);
            else
                peerInfo.addNeighbour(updatedNeighbour);
        }
        System.out.println(this + " pinged its neighbours");
    }

    public void addNeighbour()  {
        // get higher capacity host not neighbour
        Host host = peerInfo.getBestHostNotNeighbour();
        if (host == null) {
            System.out.println("There are no neighbours to add.");
            return;
        }
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
        boolean hostHigherCap = host.getCapacity() > bestNgbr.getCapacity(),
                // host has lower degree than worst neighbour (less busy)
                hostLowerDegree = host.getDegree() < worstNgbr.getDegree();

        if (hostHigherCap || hostLowerDegree)
            peerInfo.replaceNeighbour(worstNgbr, new Neighbour(host));
        // REJECT host
    }

    // To connect to the network on start
    public void addNeighbour(Neighbour neighbour) {
        peerInfo.addNeighbour(neighbour);
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
        Peer node = (Peer) o;
        return Objects.equals(peerInfo.address, node.peerInfo.address) && Objects.equals(peerInfo.port, node.peerInfo.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerInfo.address, peerInfo.port);
    }
}