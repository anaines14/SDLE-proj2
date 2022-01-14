package main.model.timelines;

import main.controller.network.NTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TimelineInfo {
    public static String FOLDER = "stored_timelines" + File.separator;
    private final File timelines_folder;
    private final Map<String, Timeline> timelines;
    private final String me;
    private final Long clockOffset;
    private int maxKeepTime; // max time to keep timeline stored (in seconds)
    private BackupService backupService;

    public TimelineInfo(String username) {
        // create folder
        this.timelines_folder = new File(FOLDER + username);
        this.timelines_folder.mkdirs();
        this.timelines = new ConcurrentHashMap<>();
        this.me = username;

        NTP ntp = new NTP();
        ntp.updateOffsets();
        this.clockOffset = ntp.getOffsetValue();
        // create own timeline file
        Timeline t = new Timeline(username, clockOffset);
        this.timelines.put(username, t);
        this.maxKeepTime = 120; // in seconds

        this.backupService = new BackupService(timelines_folder);
        // load timelines
        try {
            loadTimelines();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("ERROR: Failed to load timelines");
        }
    }

    public void showFeed() {
        List<Timeline> timelines = new ArrayList<>(this.timelines.values());
        List<Post> posts = new ArrayList<>();
        for (Timeline timeline : timelines) {
            posts.addAll(timeline.getPosts());
        }

        Collections.sort(posts);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        System.out.println("============================");
        System.out.println("\t\t\t" + this.me.toUpperCase());
        for (Post post : posts) {
            System.out.println("============================");
            System.out.printf("\t%s\t\t%s\n",post.getUsername(), post.getTimestamp().format(formatter));
            System.out.println("\t" + post.getContent());
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
        this.backup(timeline);
        this.cleanup();
    }

    public Post addPost(String username, String post_str) {

        Timeline timeline = this.timelines.computeIfAbsent(username, k -> new Timeline(username, clockOffset));
        // add post
        Post res = timeline.addPost(post_str);

        // update timeline file
        this.backup(timeline);
        this.cleanup();

        return res;
    }

    public void updatePost(String username, int postId, String newContent) {
        Timeline timeline = this.timelines.get(username);

        if (timeline == null)
            return;
        if (timeline.updatePost(postId, newContent)) {
            this.backup(timeline);
            this.cleanup();
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
            this.backup(timeline);
            this.cleanup();
        }
    }

    public void backup(Timeline timeline) {
        backupService.setTimeline(timeline);
        backupService.run();
    }

    public void cleanup() {
        LocalTime currentTime = LocalTime.now();
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

    public Long getClockOffset() {
        return clockOffset;
    }

    // for testing
    public void setMaxKeepTime(int maxKeepTime) {
        this.maxKeepTime = maxKeepTime;
    }
}
