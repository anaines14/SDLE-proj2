package main;

import main.network.GnuNode;
import timelines.Post;
import timelines.Timeline;

import java.io.*;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Peer implements Serializable{
    public static String FOLDER = "timelines" + File.separator;

    private final String username;
    private GnuNode gnunode;
    private final File timelines_folder;
    private final Map<String, Timeline> timelines;

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

    public Peer(String username) {
        this.username = username;
        // create folder
        this.timelines_folder = new File(FOLDER + username);
        this.timelines_folder.mkdirs();
        this.timelines = new HashMap<>();
        // create own timeline file
        this.timelines.put(username, new Timeline(username));
    }

    public String getUsername() { return this.username; }

    public void addPost(String post_str) {
        Timeline timeline = this.timelines.get(this.username);
        timeline.addPost(post_str);
    }

    public boolean deletePost(int postId) {
        Timeline timeline = this.timelines.get(username);
        if (timeline == null)
            return false;
        return timeline.deletePost(postId);
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

    public Map<String, Timeline> getTimelines() {
        return timelines;
    }

    public void stop() {
        this.gnunode.stop();
    }
}
