package main.network;

import main.Peer;
import main.network.executor.MultipleNodeExecutor;
import main.network.message.*;
import main.network.neighbour.Host;
import main.network.neighbour.Neighbour;
import org.zeromq.ZContext;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;

public class GnuNode {
    public static final int PINGNEIGH_DELAY = 1000;
    public static final int ADDNEIGH_DELAY = 1000;
    public static final int MAX_NGBRS = 3;

    private final PeerInfo peerInfo;
    private final InetAddress address;
    private final String port;
    private final ZContext context;
    private final MessageSender sender;
    private final MessageHandler handler;
    private int capacity; // Quantity of messages that we can handle, arbitrary for us
    private Set<Neighbour> neighbours;
    private Set<Host> hostCache;

    public GnuNode(PeerInfo peerInfo) {
        this.context = new ZContext();
        this.peerInfo = peerInfo;
        this.address = peerInfo.address;
        this.port = peerInfo.port;
        this.sender = new MessageSender(address, port, peerInfo.username, context);
        this.handler = new MessageHandler(peerInfo, context, sender);
        this.capacity = peerInfo.capacity;
        this.neighbours = peerInfo.getNeighbours(); // TODO Remove this
        this.hostCache = peerInfo.hostCache;
    }

    public void send(Message message, String port) throws IOException { sender.send(message, port); }

    public void close() {
        this.context.close();
    }

    public void join(InetAddress address, String port) {

    }

    public MessageHandler getMessageHandler() {
        return handler;
    }

    @Override
    public String toString() {
        return  address + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GnuNode node = (GnuNode) o;
        return Objects.equals(address, node.address) && Objects.equals(port, node.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }

    public static void main(String[] args) {
        try {
            PeerInfo peer1 = new PeerInfo(InetAddress.getByName("localhost"), "8884", "user1", 10);
            PeerInfo peer2 = new PeerInfo(InetAddress.getByName("localhost"), "8881", "user2", 20);
            PeerInfo peer3 = new PeerInfo(InetAddress.getByName("localhost"), "8882", "user3", 30);
            GnuNode node1 = new GnuNode(peer1);
            GnuNode node2 = new GnuNode(peer2);
            GnuNode node3 = new GnuNode(peer3);
            List<GnuNode> nodes = new ArrayList<>(Arrays.asList(node1, node2, node3));

            MultipleNodeExecutor executor = new MultipleNodeExecutor(nodes);
            executor.execute();

            node1.send(new PingMessage(), peer1.port);
            node2.send(new PingMessage(), peer2.port);
            node3.send(new PingMessage(), peer3.port);

            try {
                Thread.sleep(1000); // Wait for all messages to be sent
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("STOPPING");
            executor.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pingNeighbours() {
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
}
