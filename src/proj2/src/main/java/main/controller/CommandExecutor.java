package main.controller;

import main.Peer;
import main.controller.MultipleNodeExecutor;
import main.model.neighbour.Neighbour;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

public class CommandExecutor {
    private static final int MAX_CAPACITY = 31;
    private final MultipleNodeExecutor executor;
    private final Map<String, Peer> peers;
    private int curr_peer_id;

    public CommandExecutor() {
        this.peers = new HashMap<>();
        this.executor = new MultipleNodeExecutor();
        this.curr_peer_id = 1;
    }

    public int execCmd(String cmd) throws UnknownHostException, InterruptedException {
        String[] opts = cmd.split(" ");

        switch (opts[0].toUpperCase()) {
            case "START" :
                return this.execStart(opts);
            case "POST":
                return this.execPost(cmd, opts);
            case "STOP":
                return this.execStop(opts);
            case "START_MULT":
                return this.execStartMult(opts);
            case "STOP_ALL":
                return this.execStopAll();
            case "DELETE":
                return this.execDelete(opts);
            case "UPDATE":
                return this.execUpdate(cmd, opts);
            case "TIMELINE":
                return this.execTimeline(opts);
            case "PRINT":
                return this.execPrint(opts);
            case "PRINT_PEERS":
                return this.execPrintPeers();
            case "SLEEP":
                return this.execSleep(opts);
            case "GRAPH":
                return this.execGraph();
            case "BREAK":
                return this.execBreakpoint();
            default:
                return -1;

        }
    }

    private int execStart(String[] opts) throws UnknownHostException {
        if (opts.length < 4) return -1;

        // create and store peer
        String username = opts[1];
        InetAddress address = InetAddress.getByName(opts[2]);
        int capacity = Integer.parseInt(opts[3]);

        Peer peer = new Peer(username, address, capacity);
        startPeer(username, peer);

        return 0;
    }

    private void startPeer(String username, Peer peer) {
        this.connectToNetwork(peer);
        peers.put(username, peer);
        executor.addNode(peer);
    }

    private void connectToNetwork(Peer peer) {
        // select random peer to connect to
        if (this.peers.size() > 0) {
            List<String> users = new ArrayList<>(peers.keySet());
            String key = users.get(new Random().nextInt(users.size()));

            // add peer to neighbour list
            Peer neigh = peers.get(key);
            peer.join(new Neighbour(neigh.getPeerInfo().getHost()));
        }
    }

    private int execPost(String cmd, String[] opts) {
        if (opts.length < 3) return -1;

        // get peer
        String username = opts[1];
        Peer peer = peers.get(username);

        // split on ""
        String post_content = parsePostStr(cmd);
        if (post_content == null) return -1;

        // add post to timeline
        if (peer == null) {
            System.err.println("ERROR: Peer not found.");
            return -1;
        }

        peer.addPost(post_content);
        return 0;
    }

    private String parsePostStr(String cmd) {
        // split on first ""
        String[] cmd_split = cmd.split("\"", 2);
        if(cmd_split.length < 2) return null;

        String content = cmd_split[1];
        return content.substring(0, content.length()-1); // remove last "
    }

    private int execStop(String[] opts) {
        if (opts.length < 2) return -1;

        // remove peer
        String username = opts[1];
        Peer peer = peers.remove(username);

        // stop peer
        if (peer == null) {
            System.err.println("ERROR: Peer not found.");
            return -1;
        }
        executor.remNode(peer);
        return 0;
    }

    private int execStartMult(String[] opts) throws UnknownHostException {
        if (opts.length < 2) return -1;

        int num_peers = Integer.parseInt(opts[1]);
        InetAddress address = InetAddress.getByName("localhost");
        Random random = new Random();

        // create peers with random capacities and sequential ids
        for (int i = 1; i <= num_peers; i++) {
            String username = "user" + curr_peer_id;
            int capacity = 1 + random.nextInt(MAX_CAPACITY);

            // start and store peer
            Peer peer = new Peer(username, address, capacity);
            startPeer(username, peer);

            curr_peer_id++;
        }

        return 0;
    }

    private int execStopAll() {
        // clean map
        executor.stop();
        peers.clear();
        System.out.println("STOPPED all peers.");

        return 0;
    }

    private int execDelete(String[] opts) {
        if (opts.length < 3) return -1;

        // get peer
        String username = opts[1];
        int postId = Integer.parseInt(opts[2]);
        Peer peer = this.peers.get(username);

        if (peer == null) {
            System.err.println("ERROR: Peer not found.");
            return -1;
        }

        // delete post
        peer.deletePost(postId);
        return 0;
    }

    private int execUpdate(String cmd, String[] opts) throws NumberFormatException {
        if (opts.length < 4) return -1;

        // get peer
        String username = opts[1];
        try {
            int postId = Integer.parseInt(opts[2]);

            // split on ""
            String newContent = parsePostStr(cmd);
            if (newContent == null) return -1;

            // get peer
            Peer peer = peers.get(username);
            if (peer == null) {
                System.err.println("ERROR: Peer not found.");
                return -1;
            }

            // update post
            peer.updatePost(postId, newContent);
        } catch (NumberFormatException e) {
            return -1;
        }
        return 0;
    }

    private int execTimeline(String[] opts) { // TODO
        // get peer
        String username = opts[1];
        String timeline = opts[2];
        Peer peer = peers.get(username);

        if (peer == null) {
            System.err.println("ERROR: Peer not found.");
            return -1;
        }

        peer.pingNeighbours();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Pinged neighbours");

        peer.queryNeighbours(timeline);

        return 0;
    }

    private int execPrint(String[] opts) {
        if (opts.length < 2) return -1;

        // get peer
        String username = opts[1];
        Peer peer = peers.get(username);

        if (peer == null) {
            System.err.println("ERROR: Peer not found.");
            return -1;
        }

        // print timelines of peer
        peer.printTimelines();
        return 0;
    }

    private int execPrintPeers() {
        System.out.println("Online Peers: ");
        for (Peer peer : peers.values()) {
            System.out.println("\t" + peer);
        }
        return 0;
    }

    private int execSleep(String[] opts) throws InterruptedException {
        if (opts.length < 2) return -1;
        int time = Integer.parseInt(opts[1]) * 1000;
        Thread.sleep(time);
        return 0;
    }

    private int execGraph() {
        System.setProperty("org.graphstream.ui","swing");
        Graph graph = new SingleGraph("Network");

        // fetch file from resources
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("stylesheet");

        // set style
        graph.setAttribute("ui.stylesheet", "url('" + resource + "')");

        // create nodes
        for (String username : this.peers.keySet()) {
            Node node = graph.addNode(username);
            node.setAttribute("ui.label", node.getId());
        }
        // create edges
        for (Map.Entry<String, Peer> entry : this.peers.entrySet()) {
            String username = entry.getKey();
            Peer peer = entry.getValue();
            for (Neighbour neigh : peer.getPeerInfo().getNeighbours()) {
                String neigh_name = neigh.getUsername();
                String id = username + neigh_name;
                graph.addEdge(id, username, neigh_name);
            }
        }

        graph.display();
        System.out.println("Displayed GRAPH in external window.");
        return 0;
    }

    private int execBreakpoint() {
        System.out.println("Press enter...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
}
