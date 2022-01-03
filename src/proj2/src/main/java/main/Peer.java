package main;

import main.network.GnuNode;
import timelines.Post;
import timelines.Timeline;

import java.io.*;
import java.net.InetAddress;
import java.util.HashMap;

public class Peer implements Serializable{
    public static String FOLDER = "timelines" + File.separator;

    private final String username;
    private final GnuNode gnunode;
    private final File timelines_folder;
    private final HashMap<String, Timeline> timelines;

    public Peer(String username, InetAddress address, String port) {
        this.username = username;
        // create folder
        this.timelines_folder = new File(FOLDER + username);
        this.timelines_folder.mkdirs();
        this.timelines = new HashMap<>();
        // create own timeline file
        this.timelines.put(username, new Timeline(username));
        // join network
        this.gnunode = new GnuNode(address, port);
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

    public void stop() {
        this.gnunode.stop();
    }
}
