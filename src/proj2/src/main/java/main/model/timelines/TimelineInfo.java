package main.model.timelines;

import main.controller.network.NTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TimelineInfo {
    public static String FOLDER = "stored_timelines" + File.separator;
    private final File timelines_folder;
    private final Map<String, Timeline> timelines;
    private final String me;
    private final Long clockOffset;
    private int maxKeepTime; // max time to keep timeline stored (in seconds)

    public TimelineInfo(String username) {
        // create folder
        this.timelines_folder = new File(FOLDER + username);
        this.timelines_folder.mkdirs();
        this.timelines = new ConcurrentHashMap<>();
        this.me = username;

        NTP ntp = new NTP();
        this.clockOffset = ntp.getOffsetValue();
        // create own timeline file
        this.timelines.put(username, new Timeline(username, clockOffset));

        this.maxKeepTime = 120; // in seconds

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

    public void addTimeline(Timeline timeline) {
        LocalTime timelineToAddTimeStamp = timeline.getLastUpdate();
        Timeline t = this.getTimeline(timeline.getUsername());

        //Returns if saved timeline is more recent
        if(t != null) {
            LocalTime savedTimelineTimeStamp = t.getLastUpdate();
            if(timelineToAddTimeStamp.compareTo(savedTimelineTimeStamp) <= 0) {
                return;
            }
        }

        this.timelines.put(timeline.getUsername(), timeline);
        try {
            // save timeline in non volatile memory
            timeline.save(this.timelines_folder);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't save timeline: " + timeline.getUsername());
        }
        this.cleanup();
    }

    public Post addPost(String username, String post_str) {
        // add post
        Timeline timeline = this.timelines.get(username);
        Post res = timeline.addPost(post_str);
        // update timeline file
        try {
            timeline.save(this.timelines_folder);
            this.cleanup();
        } catch (IOException e) {
            System.err.println("ERROR: Failed to save timeline.");
        }
        return res;
    }

    public void updatePost(String username, int postId, String newContent) {
        Timeline timeline = this.timelines.get(username);

        if (timeline == null)
            return;
        if (timeline.updatePost(postId, newContent)) {
            // update timeline
            try {
                timeline.save(this.timelines_folder);
                this.cleanup();
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
                this.cleanup();
            } catch (IOException e) {
                System.err.println("ERROR: Failed to save timeline.");
            }
        }
    }

    public void cleanup() {
        LocalTime currentTime = LocalTime.now(); // TODO: ntp
        currentTime = currentTime.plusNanos(clockOffset);

        for (Timeline timeline : this.timelines.values()) {
            // don't delete own timeline
            if (timeline.getUsername().equals(me))
                continue;

            long diff = Duration.between(timeline.getLastUpdate(), currentTime).toSeconds(); // TODO: use minutes maybe?

            // remove timeline if is older than specified time
            if (diff > maxKeepTime){
                System.out.println(me + " CLEANUP " + timeline.getUsername());
                this.timelines.remove(timeline.getUsername());
                try {
                    if (!timeline.remove(this.timelines_folder)) {
                        System.out.println("Couldn't delete timeline: " + timeline.getUsername());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    public Set<String> getStoredTimelines() {
        return this.timelines.keySet();
    }

    // for testing
    public void setMaxKeepTime(int maxKeepTime) {
        this.maxKeepTime = maxKeepTime;
    }
}
