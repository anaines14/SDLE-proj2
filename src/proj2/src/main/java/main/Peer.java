package main;

import timelines.Post;
import timelines.Timeline;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class Peer implements Serializable {
    public static String FOLDER = "timelines" + File.separator;

    private final String username;
    private final File timelines_folder;
    private final HashMap<String, Timeline> timelines;

    public Peer(String username) {
        this.username = username;
        // create folder
        this.timelines_folder = new File(FOLDER + username);
        this.timelines_folder.mkdirs();
        this.timelines = new HashMap<>();
        // create own timeline file
        this.timelines.put(username, new Timeline(username));
    }

    public static void main(String[] args) {
        if(args.length < 1) {
            usage();
            System.exit(1);
        }
        // create peer
        Peer peer = new Peer(args[0]);

        // command loop
        Scanner scanner = new Scanner(System.in);
        String cmd;
        System.out.println("Started Peer \n\tusername: " + peer.username + "\n\nPlease enter a command.");
        do {
            cmd = scanner.nextLine();
            String[] opts = cmd.split(" ");

            switch (opts[0].toUpperCase()) {
                case "POST":
                    // add post to timeline
                    String post_str = cmd.replaceFirst(opts[0] + " ", "");
                    peer.addPost(post_str);
                    System.out.println("Added post: \"" + post_str + "\"");
                    break;
                case "STOP":
                    System.out.println("Stopping peer.");
                    break;
                default:
                    System.out.println("Unknown command.");
            }
        } while (!cmd.equalsIgnoreCase("STOP"));
        System.exit(0);
    }

    private static void usage() {
        System.out.println("usage: Peer.java <username>");
    }

    public void addPost(String post_str) {
        Timeline timeline = this.timelines.get(this.username);
        Post post = new Post(post_str);
        timeline.addPost(post);
    }

    public void saveTimelines() throws IOException {
        for(Timeline t : timelines.values()) {
            t.save(this.timelines_folder);
        }
    }

    public void loadTimelines() throws IOException, ClassNotFoundException {
        File[] files = timelines_folder.listFiles();

        if (files == null)
            return;

        for(File f : files) {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Timeline timeline = (Timeline)ois.readObject(); // down-casting object
            this.timelines.put(timeline.getUsername(), timeline);

            ois.close();
        }
    }

    public HashMap<String, Timeline> getTimelines() {
        return timelines;
    }
}
