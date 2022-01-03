package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TestApp {
    private static final String TESTS_DIR = "src" + File.separator +
            "main" + File.separator + "resources" + File.separator;

    private final Map<String, Peer> peers;
    private int curr_peer_id;

    public TestApp() {
        this.peers = new HashMap<>();
        this.curr_peer_id = 1;
    }

    public static void main(String[] args) {
        // check number of arguments
        if (args.length != 1) {
            usage();
            System.exit(1);
        }

        TestApp app = new TestApp();
        String filename = args[0];
        app.run_test(filename);
    }

    private void run_test(String filename) {
        File testFile = new File(TESTS_DIR + filename);

        try {
            Scanner scanner = new Scanner(testFile);
            do {
                String cmd = scanner.nextLine();
                String[] opts = cmd.split(" ");

                switch (opts[0].toUpperCase()) {
                    case "START":
                        this.execStart(opts);
                        break;
                    case "POST":
                        this.execPost(cmd, opts);
                        break;
                    case "STOP":
                        this.execStop(opts);
                        break;
                    case "START_MULT":
                        this.execStartMult(opts);
                        break;
                    case "STOP_ALL":
                        this.execStopAll(opts);
                        break;
                    case "DELETE":
                        this.execDelete(opts);
                    default:
                        System.out.println("Unknown command.");
                        System.exit(1);
                }
            } while(scanner.hasNext());

        } catch (FileNotFoundException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void execStart(String[] opts) throws UnknownHostException {
        if (opts.length < 4) {
            System.out.println("usage: START <username> <address> <port>");
            System.exit(1);
        }

        // create and store peer
        String username = opts[1];
        InetAddress address = InetAddress.getByName(opts[2]);
        String port = opts[3];
        peers.put(opts[1], new Peer(username, address, port));
    }

    private void execPost(String cmd, String[] opts) throws UnknownHostException {
        if (opts.length < 3) {
            System.out.println("usage: POST <username> \"<content>\"");
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
            System.out.println("usage: STOP <username>");
            System.exit(1);
        }

        // get peer
        String username = opts[1];
        Peer peer = peers.get(username);
        // stop peer
        peer.stop();
    }

    private void execStartMult(String[] opts) throws UnknownHostException {
        if (opts.length < 2) {
            System.out.println("usage: START_MULT <n>");
            System.exit(1);
        }

        int num_peers = Integer.parseInt(opts[1]);
        InetAddress user_addr = InetAddress.getByName("localhost");

        // start and store peers
        for (int i = 1; i <= num_peers; i++) {
            String username = "user" + curr_peer_id;
            peers.put(username, new Peer(username, user_addr, String.valueOf(8000 + curr_peer_id)));
            curr_peer_id++;
        }
    }

    private void execStopAll(String[] opts) throws UnknownHostException {
        // stop all peers
        for (Peer p : peers.values()) {
            p.stop();
        }
    }

    private void execDelete(String[] opts) throws UnknownHostException {
        if (opts.length < 2) {
            System.out.println("usage: DELETE <username> <post_id>");
            System.exit(1);
        }

        // get peer
        String username = opts[1];
        Peer peer = this.peers.get(username);
        int postId = Integer.parseInt(opts[2]);
        // delete post
        peer.deletePost(postId);
    }

    private static void usage() {
        System.out.println("usage: TestApp.java <test_file>");
    }
}
