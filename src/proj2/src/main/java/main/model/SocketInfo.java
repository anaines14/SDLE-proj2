package main.model;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZSocket;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Keeps in storage information about the Peer's public Sockets
// Useful to tell other peers which sockets to connect
public class SocketInfo {
    private ZContext context;
    private InetAddress address;
    private ZMQ.Socket frontend;
    private ZMQ.Socket publisher;
    private String publisherPort;
    private String frontendPort;

    private Map<String, ZMQ.Socket> subscriptions; // Connects to all nodes that we have subscribed to
    private Map<String, ZMQ.Socket> redirects; // Nodes to which we have to send post from subscribers
    private Map<String, String> redirectPorts; // username => port

    public SocketInfo(String publisherPort, String frontendPort) { // For testing
        this.publisherPort = publisherPort;
        this.frontendPort = frontendPort;
    }

    public SocketInfo(ZContext context, InetAddress address, SocketType frontendType, SocketType publisherType) {
        this.context = context;
        this.address = address;
        String hostName = address.getHostName();

        this.frontend = context.createSocket(frontendType);
        this.publisher = context.createSocket(publisherType);

        this.frontendPort = String.valueOf(frontend.bindToRandomPort("tcp://" + hostName));
        this.publisherPort = String.valueOf(publisher.bindToRandomPort("tcp://" + hostName));

        this.subscriptions = new ConcurrentHashMap<>();
        this.redirects = new ConcurrentHashMap<>();
        this.redirectPorts = new ConcurrentHashMap<>();
    }

    public String getPublisherPort() {
        return publisherPort;
    }

    public String getFrontendPort() {
        return frontendPort;
    }

    public ZMQ.Socket getPublisher() {
        return publisher;
    }

    public ZMQ.Socket getFrontend() {
        return frontend;
    }

    public boolean isSubscribed(String username) {
        return this.subscriptions.containsKey(username);
    }

    public Collection<ZMQ.Socket> getSubscriptions() {
        return subscriptions.values();
    }

    public Map<String, ZMQ.Socket> getRedirects() { return redirects; }

    public boolean hasRedirect(String username) { return this.redirects.containsKey(username); }

    public ZMQ.Socket getRedirectSocket(String username) { return this.redirects.get(username); }

    public Set<String> getSubsribedUsers() {
        return subscriptions.keySet();
    }

    public int getSubscriptionSize() {
        return subscriptions.size();
    }

    public void addSubscription(String username, InetAddress address, String port) {
        ZMQ.Socket subscription = context.createSocket(SocketType.SUB);
        String hostName = address.getHostName();
        subscription.connect("tcp://" + hostName + ":" + port);
        System.out.println("SUBBED TO " + "tcp://" + hostName + ":" + port);
        subscription.subscribe("".getBytes());
        this.subscriptions.put(username, subscription);
    }

    public void removeSubscription(String username) {
        ZMQ.Socket subscription = subscriptions.get(username);
        subscription.setLinger(0);
        subscription.close();
        subscriptions.remove(username);
    }

    public String addRedirect(String username, InetAddress address) {

        ZMQ.Socket pub = context.createSocket(SocketType.PUB);
        String hostName = address.getHostName();
        int p = pub.bindToRandomPort("tcp://" + hostName);
        String port = Integer.toString(p);
        this.redirects.put(username, pub);
        this.redirectPorts.put(username, port);
        System.out.println("ADDING REDIRECT =========================== " + port + " " + username);
        return port;
    }

    public void close() {
        frontend.close();
        publisher.close();
        for (ZMQ.Socket subscriber: subscriptions.values())
            subscriber.close();
        for (ZMQ.Socket redirect: redirects.values())
            redirect.close();
    }
}
