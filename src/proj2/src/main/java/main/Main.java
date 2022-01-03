package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String filename = args[0];
        File file = new File("src/main/resources/" + filename);
        HashMap<String, Peer> peers = new HashMap<>();

        try {
            Scanner scanner = new Scanner(file);
            do {
                String cmd = scanner.nextLine();
                String[] opts = cmd.split(" ");

                if (opts.length < 2) {
                    usage(cmd);
                    System.exit(1);
                }

                String username = opts[1];

                switch (cmd.toUpperCase()) {
                    case "START":
                        peers.put(username, new Peer(username));

                        //TODO: Start thread with peer
                        break;
                    case "POST":
                        Peer peer = peers.get(username);
                        break;
                }
            } while(scanner.hasNext());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void usage(String cmd) {
        System.out.println("Invalid command: " + cmd);
    }
}
