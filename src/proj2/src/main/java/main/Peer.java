package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

public class Peer implements Serializable {
    public static String FOLDER = "_TIMELINES";

    private final String username;
    private final File timelines_folder;
    private HashMap<String, Timeline> timelines;
    private InetAddress ipaddress;
    private int port;
    private int capacity;
    private List<String> neighbors;
    private HashMap<InetAddress, Integer> hostcache; // ip:port => capacity

    public Peer(String username) {
        this.username = username;
        this.timelines_folder = new File(username + FOLDER);
        this.timelines_folder.mkdir();
        this.timelines = new HashMap<>();

        this.timelines.put(username, new Timeline(username));
    }

    public void addPost(String post_str) {
        Timeline timeline = this.timelines.get(this.username);
        Post post = new Post(post_str);
        timeline.addPost(post);
    }

    public void save() {

    }
}
