package main;

import main.model.PeerInfo;
import main.controller.network.Broker;
import main.controller.message.MessageSender;
import main.model.message.MessageResponse;
import main.model.message.PingMessage;
import main.model.message.PongMessage;
import main.model.message.QueryMessage;
import main.model.neighbour.Host;
import main.model.neighbour.Neighbour;
import main.model.timelines.TimelineInfo;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;
import java.util.Set;
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

    public Peer(String username, InetAddress address, int capacity) {
        this.context = new ZContext();

        // assign random port
        ZMQ.Socket socket = context.createSocket(SocketType.REP);
        int p = socket.bindToRandomPort("tcp://" + address.getHostName());
        String port = Integer.toString(p);
        socket.close();

        this.peerInfo = new PeerInfo(address, port, username, capacity);
        this.timelineInfo = new TimelineInfo(username);
        this.sender = new MessageSender(peerInfo, context);
        this.broker = new Broker(context, sender, peerInfo);
    }

    public void join(Neighbour neighbour) {
        peerInfo.addNeighbour(neighbour);
    }

    public PongMessage ping(Neighbour neighbour) {
        MessageResponse response = null;
        int i = 0;
        boolean done = false;
        while (i < 3 && !done) {
            response = this.sender.
                    sendRequest(new PingMessage(peerInfo.getHost()), neighbour.getPort(), RCV_TIMEOUT);

            if (response != null && Objects.equals(response.getType(), "PONG")) {
                done = true;
            }

            ++i;
        }

        return (PongMessage) response;
    }

    public void queryNeighbour(String wantedTimeline, Neighbour neighbour) {
        this.sender.sendRequest(new QueryMessage(wantedTimeline, peerInfo.getHost()), neighbour.getPort());
    }

    public void printTimelines() {
        this.timelineInfo.printTimelines();
    }

    public void updatePost(int postId, String newContent) {
        this.timelineInfo.updatePost(peerInfo.getUsername(), postId, newContent);
    }

    public void addPost(String newContent) {
        this.timelineInfo.addPost(peerInfo.getUsername(), newContent);
    }

    public void deletePost(int postId) {
        this.timelineInfo.deletePost(peerInfo.getUsername(), postId);
    }

    public void execute(ScheduledThreadPoolExecutor scheduler) {
        this.broker.execute();
        pingNeigFuture = scheduler.scheduleWithFixedDelay(this::pingNeighbours,
                0, PINGNEIGH_DELAY, TimeUnit.MILLISECONDS);
        addNeighFuture = scheduler.scheduleWithFixedDelay(this::addNeighbour,
                0, ADDNEIGH_DELAY, TimeUnit.MILLISECONDS);
    }

    public void cancelHooks() {
        if (pingNeigFuture != null) pingNeigFuture.cancel(false);
        if (addNeighFuture != null) addNeighFuture.cancel(false);
    }

    public void stop() {
        this.broker.stop();
        this.cancelHooks();
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
            PongMessage response = ping(neighbour);
            if (response == null) { // Went offline after n tries
                System.out.println(peerInfo.getUsername() + " REMOVED " + neighbour.getUsername());
                peerInfo.removeNeighbour(neighbour);
                peerInfo.removeHost(neighbour);
                continue;
            }

            Neighbour updatedNeighbour = response.sender;
            if (peerInfo.hasNeighbour(updatedNeighbour)) {
                Set<Host> hostCache = response.hostCache;
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
        boolean hostHigherCap = host.getCapacity() > bestNgbr.getCapacity(),
                // host has lower degree than worst neighbour (less busy)
                hostLowerDegree = host.getDegree() < worstNgbr.getDegree();

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