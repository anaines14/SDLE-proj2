package main;

import main.network.GnuNode;
import main.timelines.Timeline;

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
        // load main.timelines
        try {
            loadTimelines();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("ERROR: Failed to load main.timelines");
        }
        // join network
        this.gnunode = new GnuNode(address, port, 0); // TODO add capacity

        System.out.println("STARTED peer.\n\tusername: " + username +
                "\n\tIPaddress: " + address.toString() + "\n\tPort: " + port);
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

    public void addPost(String post_str) {
        // add post
        Timeline timeline = this.timelines.get(this.username);
        timeline.addPost(post_str);
        // update timeline file
        try {
            saveTimelines();
        } catch (IOException e) {
            System.err.println("ERROR: Failed to save timeline.");
        }
    }

    public void updatePost(int postId, String newContent) {
        Timeline timeline = this.timelines.get(username);

        if (timeline == null)
            return;
        if (timeline.updatePost(postId, newContent)) {
            // update timeline
            try {
                saveTimelines();
            } catch (IOException e) {
                System.err.println("ERROR: Failed to save timeline.");
            }
        }
    }

    public void deletePost(int postId) {
        // get timeline
        Timeline timeline = this.timelines.get(username);
        if (timeline == null)
            return;
        // delete post
        if (timeline.deletePost(postId)) {
            // update timeline
            try {
                saveTimelines();
            } catch (IOException e) {
                System.err.println("ERROR: Failed to save timeline.");
            }
        }
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

    public void printTimeline() {
        for (Timeline timeline : this.timelines.values())
            System.out.println(timeline);
    }

    public void stop() {
        this.gnunode.close();
        System.out.println("STOPPED: " + username + ".");
    }

    @Override
    public String toString() {
        return username + " " + gnunode;
    }
}
