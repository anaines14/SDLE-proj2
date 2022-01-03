package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

public class TestApp {
    private static String TESTS_DIR = "src" + File.separator + "main" + File.separator + "resources" + File.separator;

    public static void main(String[] args) {
        if (args.length < 1) {
            usage();
            System.exit(1);
        }
        String filename = args[0];
        File file = new File(TESTS_DIR+ filename);
        HashMap<String, Peer> peers = new HashMap<>();

        int curr_id = 1;

        try {
            Scanner scanner = new Scanner(file);
            do {
                String cmd = scanner.nextLine();
                String[] opts = cmd.split(" ");


                Peer peer = null;
                switch (opts[0].toUpperCase()) {
                    case "START":
                        if (opts.length < 4) {
                            usage(cmd);
                            System.exit(1);
                        }

                        InetAddress address = InetAddress.getByName(opts[2]);

                        peers.put(opts[1], new Peer(opts[1], address, opts[3]));
                        break;
                    case "POST":
                        if (opts.length < 3) {
                            usage(cmd);
                            System.exit(1);
                        }

                        peer = peers.get(opts[1]);

                        // split on first ""
                        String post_content = cmd.split("\"", 2)[1];
                        post_content = post_content.substring(0, post_content.length()-1); // remove last "

                        peer.addPost(post_content);
                        break;
                    case "STOP":
                        peer = peers.get(opts[1]);
                        peer.stop();
                        break;
                    case "START_MULT":
                        int num_peers = Integer.parseInt(opts[1]);
                        InetAddress user_addr = InetAddress.getByName("localhost");
                        for (int i = 1; i <= num_peers; i++) {
                            String name = "user" + curr_id;
                            peers.put(name, new Peer(name, user_addr, String.valueOf(8080 + curr_id)));
                            curr_id++;
                            System.out.println(curr_id);
                        }
                        break;
                    case "STOP_ALL":
                        for (Peer p : peers.values()) {
                            p.stop();
                        }
                        break;
                    default:
                        usage(cmd);
                        System.exit(1);
                }
            } while(scanner.hasNext());

        } catch (FileNotFoundException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static void usage(String cmd) {
        System.out.println("Invalid command: " + cmd);
    }

    private static void usage() {
        System.out.println("usage: TestApp.java <test_file>");
    }
}
