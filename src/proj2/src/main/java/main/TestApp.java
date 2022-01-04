package main;

import main.network.Peer;
import main.network.executor.MultipleNodeExecutor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TestApp {
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

        System.exit(0);
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
            case "PRINT" -> this.execPrint(opts);
            case "PRINT_PEERS" -> this.execPrintPeers();
            case "SLEEP" -> this.execSleep(opts);
            case "BREAK" -> this.execBreakpoint();
            default -> {
                System.out.println("Unknown command.\n");
                usage();
                System.exit(1);
            }
        }
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
        peers.put(username, peer);
        executor.addNode(peer);
    }

    private void execPost(String cmd, String[] opts) throws UnknownHostException {
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
        peer.addPost(post_content);
    }

    private void execStop(String[] opts) throws UnknownHostException {
        if (opts.length < 2) {
            usage();
            System.exit(1);
        }

        // remove peer
        String username = opts[1];
        Peer peer = peers.remove(username);
        // stop peer
        executor.remNode(peer);
    }

    private void execStartMult(String[] opts) throws UnknownHostException {
        if (opts.length < 2) {
            usage();
            System.exit(1);
        }

        int num_peers = Integer.parseInt(opts[1]);
        InetAddress user_addr = InetAddress.getByName("localhost");

        // TODO: Gen random caps
        // start and store peers
        for (int i = 1; i <= num_peers; i++) {
            String username = "user" + curr_peer_id;
            Peer peer = new Peer(username, user_addr, String.valueOf(8000 + curr_peer_id), 0);
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

    private void execDelete(String[] opts) throws UnknownHostException {
        if (opts.length < 3) {
            usage();
            System.exit(1);
        }

        // get peer
        String username = opts[1];
        Peer peer = this.peers.get(username);
        int postId = Integer.parseInt(opts[2]);
        // delete post
        peer.deletePost(postId);
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
        System.out.println("usage: TestApp.java <test_file>" +
                "\n\nAvalable commands:\n\n" +
                "\n\t START <username> <IPaddress> <port> <capacity>" +
                "\n\t START_MULT <n>" +
                "\n\t POST <username> \"<content>\"" +
                "\n\t UPDATE <username> <post_id> \"<content>\"" +
                "\n\t DELETE <username> <post_id>" +
                "\n\t PRINT <username>" +
                "\n\t PRINT_PEERS" +
                "\n\t STOP <username>" +
                "\n\t STOP_ALL" +
                "\n\t BREAK" +
                "\n\t SLEEP <seconds>");
    }
}
