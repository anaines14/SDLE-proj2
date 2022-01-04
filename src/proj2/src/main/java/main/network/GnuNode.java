package main.network;

import main.network.executor.MultipleNodeExecutor;
import main.network.message.*;
import main.network.neighbour.Host;
import main.network.neighbour.Neighbour;
import org.zeromq.ZContext;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

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
        System.out.println(this.toString() + " pinged its neighbours");
    }

    public void addNeighbour()  {
        // get higher capacity host not neighbour
        Host host = this.getBestHostNotNeighbor();
        if(host == null) {
            System.out.println("There are no neighbours to add.");
            return;
        }
        // ACCEPT host if limit not reached
        if(neighbours.size() < MAX_NGBRS) {
            neighbours.add(new Neighbour(host));
            return;
        }

        // from neighbours with less capacity than host, get the one with max degree
        Neighbour worstNgbr = this.getWorstNeighbor(host.getCapacity());
        if(worstNgbr == null) return; // REJECT host if there are no worse neighbours

        // get highest capacity node
        Neighbour bestNgbr = neighbours.stream().
                max(Comparator.comparingInt(Neighbour::getCapacity)).get();

        // host has higher capacity than every neighbour
        boolean hostHigherCap = host.getCapacity() > bestNgbr.getCapacity(),
                // host has lower degree than worst neighbour (less busy)
                hostLowerDegree = host.getDegree() < worstNgbr.getDegree();

        if (hostHigherCap || hostLowerDegree) {
            neighbours.remove(worstNgbr); // remove worst neighbour
            neighbours.add(new Neighbour(host)); // ACCEPT host as new neighbour
        }
        // REJECT host
    }

    private Neighbour getWorstNeighbor(int hostCapacity) {
        // get neighbors with less capacity than host
        List<Neighbour> badNgbrs = neighbours.stream()
                .filter(n -> n.getCapacity() < hostCapacity).toList();
        if (badNgbrs.isEmpty()) return null; // REJECT host if there are no worse neighbours

        // from neighbours with less capacity than host, get the one with max degree
        return badNgbrs.stream().max(Host::compareTo).get();
    }

    private Host getBestHostNotNeighbor() {
        // filter already neighbors
        Set<Host> notNeighbors = hostCache.stream()
                .filter(f -> !neighbours.contains(f))
                .collect(Collectors.toSet());

        Optional<Host> best_host = notNeighbors.stream().max(Comparator.comparingInt(Host::getCapacity));
        if(best_host.isEmpty()) return null;

        return best_host.get();
    }

    // TODO: DELETE
    private void test() throws UnknownHostException {
        InetAddress addr = InetAddress.getByName("localhost");
        neighbours.add(new Neighbour("1",addr,"8000",10,10));
        neighbours.add(new Neighbour("2",addr,"8001",5,10));

        hostCache.add(new Host("1",addr,"8000",10,10));
        hostCache.add(new Host("2",addr,"8001",5,10));
        hostCache.add(new Host("3",addr,"8002",3,7));
        hostCache.add(new Host("4",addr,"8003",3,10));

    }
}
