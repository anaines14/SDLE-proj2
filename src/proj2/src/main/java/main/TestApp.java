package main;

import main.network.Peer;
import main.network.executor.MultipleNodeExecutor;
import main.network.neighbour.Neighbour;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

public class TestApp {
    private static final int MAX_CAPACITY = 31;
    private final MultipleNodeExecutor executor;
    private final Map<String, Peer> peers;
    private int curr_peer_id;

    public TestApp() {
        this.peers = new HashMap<>();
        this.executor = new MultipleNodeExecutor();
        this.curr_peer_id = 1;
    }

    public static void main(String[] args) {
        TestApp app = new TestApp();

        // check number of arguments
        if (args.length == 1) { // run test from file
            String filename = args[0];
            app.run_test(filename);
        }
        else { // run loop using user input
            app.run_loop();
        }
    }

    private void run_loop() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter a command (Type EXIT to end)...");
        String cmd = scanner.nextLine();
        while (!cmd.equalsIgnoreCase("EXIT")) {
            try {
                execCmd(cmd); // exec command
                // get command
                cmd = scanner.nextLine();

            } catch (UnknownHostException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void run_test(String filename) {
        // fetch file from resources
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(filename);

        try {
            assert resource != null;
            File testFile = new File(resource.toURI());
            Scanner scanner = new Scanner(testFile);
            do {
                // get command
                String cmd = scanner.nextLine();
                execCmd(cmd); // exec command
            } while(scanner.hasNextLine());

        } catch (FileNotFoundException | UnknownHostException | InterruptedException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void connectToNetwork(Peer peer) {
        // select random peer to connect to
        if (this.peers.size() > 0) {
            Random rand = new Random();
            List<String> users = new ArrayList<>(peers.keySet());
            String key = users.get(rand.nextInt(users.size()));
            // add peer to neighbour list
            Peer neigh = peers.get(key);
            peer.addNeighbour(new Neighbour(neigh.getPeerInfo()));
        }
    }

    private void execCmd(String cmd) throws UnknownHostException, InterruptedException {
        String[] opts = cmd.split(" ");

        switch (opts[0].toUpperCase()) {
            case "START" -> this.execStart(opts);
            case "POST" -> this.execPost(cmd, opts);
            case "STOP" -> this.execStop(opts);
            case "START_MULT" -> this.execStartMult(opts);
            case "STOP_ALL" -> this.execStopAll();
            case "DELETE" -> this.execDelete(opts);
            case "UPDATE" -> this.execUpdate(cmd, opts);
            case "TIMELINE" -> this.execTimeline(opts);
            case "PRINT" -> this.execPrint(opts);
            case "PRINT_PEERS" -> this.execPrintPeers();
            case "SLEEP" -> this.execSleep(opts);
            case "GRAPH" -> this.execGraph();
            case "BREAK" -> this.execBreakpoint();
            default -> {
                System.out.println("Unknown command.\n");
                usage();
                System.exit(1);
            }
        }
    }

    private void execTimeline(String[] opts) {
        // get peer
        String username = opts[1];
        String timeline = opts[2];
        Peer peer = peers.get(username);

        if (peer != null)
            peer.getTimelineFrom(timeline);
    }

    private void execGraph() {
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
    }

    private void execPrint(String[] opts) {
        if (opts.length < 2) {
            usage();
            System.exit(1);
        }
        // get peer
        String username = opts[1];
        Peer peer = peers.get(username);
        // print timeline
        if (peer != null)
            peer.printTimelines();
    }

    private void execUpdate(String cmd, String[] opts) throws NumberFormatException {
        if (opts.length < 4) {
            usage();
            System.exit(1);
        }
        // get peer
        String username = opts[1];
        try {
            int postId = Integer.parseInt(opts[2]);

            // split on first ""
            String[] cmd_split = cmd.split("\"", 2);
            if(cmd_split.length < 2) {
                usage();
                System.exit(1);
            }
            String newContent = cmd_split[1];
            newContent = newContent.substring(0, newContent.length()-1); // remove last "

            Peer peer = peers.get(username);
            // update post
            if (peer != null)
                peer.updatePost(postId, newContent);
        } catch (NumberFormatException e) {
            usage();
            System.exit(1);
        }
    }

    private void execStart(String[] opts) throws UnknownHostException {
        if (opts.length < 5) {
            usage();
            System.exit(1);
        }

        // create and store peer
        String username = opts[1];
        InetAddress address = InetAddress.getByName(opts[2]);
        String port = opts[3];
        int capacity = Integer.parseInt(opts[4]);

        Peer peer = new Peer(username, address, port, capacity);

        this.connectToNetwork(peer);
        peers.put(username, peer);
        executor.addNode(peer);
    }

    private void execPost(String cmd, String[] opts) {
        if (opts.length < 3) {
            usage();
            System.exit(1);
        }

        // get peer
        String username = opts[1];
        Peer peer = peers.get(username);

        // split on first ""
        String post_content = cmd.split("\"", 2)[1];
        post_content = post_content.substring(0, post_content.length()-1); // remove last "

        // add post to timeline
        if (peer != null)
            peer.addPost(post_content);
    }

    private void execStop(String[] opts) {
        if (opts.length < 2) {
            usage();
            System.exit(1);
        }

        // remove peer
        String username = opts[1];
        Peer peer = peers.remove(username);
        // stop peer
        if (peer != null)
            executor.remNode(peer);
    }

    private void execStartMult(String[] opts) throws UnknownHostException {
        if (opts.length < 2) {
            usage();
            System.exit(1);
        }

        int num_peers = Integer.parseInt(opts[1]);
        InetAddress user_addr = InetAddress.getByName("localhost");

        Random random = new Random();
        // start and store peers
        for (int i = 1; i <= num_peers; i++) {
            String username = "user" + curr_peer_id;
            int random_cap = 1 + random.nextInt(MAX_CAPACITY);
            Peer peer = new Peer(username, user_addr, String.valueOf(8000 + curr_peer_id), random_cap);
            this.connectToNetwork(peer);
            peers.put(username, peer);
            executor.addNode(peer);
            curr_peer_id++;
        }
    }

    private void execStopAll() {
        // clean map
        executor.stop();
        peers.clear();
    }

    private void execDelete(String[] opts) {
        if (opts.length < 3) {
            usage();
            System.exit(1);
        }

        // get peer
        String username = opts[1];
        Peer peer = this.peers.get(username);
        if (peer != null) {
            int postId = Integer.parseInt(opts[2]);
            // delete post
            peer.deletePost(postId);
        }
    }

    private void execSleep(String[] opts) throws InterruptedException {
        if (opts.length < 2) {
            usage();
            System.exit(1);
        }
        int time = Integer.parseInt(opts[1]) * 1000;
        Thread.sleep(time);
    }

    private void execPrintPeers() {
        System.out.println("Online Peers: ");
        for (Peer peer : peers.values()) {
            System.out.println("\t" + peer);
        }
    }

    private void execBreakpoint() {
        System.out.println("Press enter...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void usage() {
        System.out.println("""
                usage: TestApp.java <test_file>

                Avalable commands:


                \t START <username> <IPaddress> <port> <capacity>
                \t START_MULT <n>
                \t POST <username> "<content>"
                \t UPDATE <username> <post_id> "<content>"
                \t DELETE <username> <post_id>
                \t TIMELINE <username>
                \t PRINT <username>
                \t PRINT_PEERS
                \t STOP <username>
                \t STOP_ALL
                \t GRAPH
                \t BREAK
                \t SLEEP <seconds>""");
    }
}
