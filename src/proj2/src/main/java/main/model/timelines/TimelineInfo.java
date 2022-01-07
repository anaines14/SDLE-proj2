package main.model.timelines;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

public class TimelineInfo {
    public static String FOLDER = "timelines" + File.separator;
    private final File timelines_folder;
    private final Map<String, Timeline> timelines;

    public TimelineInfo(String username) {
        // create folder
        this.timelines_folder = new File(FOLDER + username);
        this.timelines_folder.mkdirs();
        this.timelines = new HashMap<>();

        // create own timeline file
        this.timelines.put(username, new Timeline(username));

        // load timelines
        try {
            loadTimelines();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("ERROR: Failed to load timelines");
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

    public void addPost(String username, String post_str) {
        // add post
        Timeline timeline = this.timelines.get(username);
        timeline.addPost(post_str);
        // update timeline file
        try {
            timeline.save(this.timelines_folder);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to save timeline.");
        }
    }

    public void updatePost(String username, int postId, String newContent) {
        Timeline timeline = this.timelines.get(username);

        if (timeline == null)
            return;
        if (timeline.updatePost(postId, newContent)) {
            // update timeline
            try {
                timeline.save(this.timelines_folder);
            } catch (IOException e) {
                System.err.println("ERROR: Failed to save timeline.");
            }
        }
    }

    public void deletePost(String username, int postId) {
        // get timeline
        Timeline timeline = this.timelines.get(username);
        if (timeline == null)
            return;
        // delete post
        if (timeline.deletePost(postId)) {
            // update timeline
            try {
                timeline.save(this.timelines_folder);
            } catch (IOException e) {
                System.err.println("ERROR: Failed to save timeline.");
            }
        }
    }

    public void printTimelines() {
        for (Timeline timeline : this.timelines.values())
            System.out.println(timeline);
    }

    public boolean hasTimeline(String wantedUser) {
        return timelines.containsKey(wantedUser);
    }

    public Timeline getTimeline(String wantedUser) {
        return this.timelines.get(wantedUser);
    }
}
