package main.network;

import main.network.executor.MultipleNodeExecutor;
import main.network.message.Message;
import main.network.message.MessageBuilder;
import main.network.message.MessageHandler;
import main.network.neighbour.Host;
import main.network.neighbour.Neighbour;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GnuNode {
    public static final int PINGNEIGH_DELAY = 1000;
    public static final int ADDNEIGH_DELAY = 1000;

    private final InetAddress address;
    private final String port;
    private final ZContext context;
    private final MessageHandler handler;
    private int capacity; // Quantity of messages that we can handle, arbitrary for us
    private List<Neighbour> neighbours;
    private List<Host> hostCache;

    public GnuNode(InetAddress address, String port, Integer capacity) {
        this.context = new ZContext();
        this.address = address;
        this.port = port;
        this.handler = new MessageHandler(address, port, context);
        this.capacity = capacity;
        this.neighbours = new ArrayList<>();
        this.hostCache = new ArrayList<>();
    }

    public void send(Message message, String port) throws IOException {
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.connect("tcp://localhost:" + port); // TODO convert to address

        byte[] bytes = MessageBuilder.messageToByteArray(message);
        socket.send(bytes);
        socket.close();
    }

    public void close() {
        this.context.close();
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getPort() {
        return port;
    }

    public Integer getDegree() {
        return this.neighbours.size();
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
            GnuNode node1 = new GnuNode(InetAddress.getByName("localhost"), "8884", 10);
            GnuNode node2 = new GnuNode(InetAddress.getByName("localhost"), "8881", 20);
            GnuNode node3 = new GnuNode(InetAddress.getByName("localhost"), "8882", 30);
            List<GnuNode> nodes = new ArrayList<>(Arrays.asList(node1, node2, node3));

            MultipleNodeExecutor executor = new MultipleNodeExecutor(nodes);
            executor.execute();

            node1.send(new Message("a", "b"), node2.getPort());
            node2.send(new Message("c", "d"), node3.getPort());
            node3.send(new Message("e", "g"), node1.getPort());

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

    public void addNeighbour() {
        System.out.println(this.toString() + " adds a neighbour");
    }
}
