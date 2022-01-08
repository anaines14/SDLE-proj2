package main.model.timelines;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Timeline implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<Integer, Post> posts;
    private int lastPostId;
    private final String username;
    private LocalTime lastUpdate;
    private final Long clockOffset;

    public Timeline(String username, Long clockOffset) {
        this.posts = new HashMap<>();
        this.username = username;
        this.lastUpdate = LocalTime.now().plusNanos(clockOffset);
        this.lastPostId = 0;
        this.clockOffset = clockOffset;
    }

    public void addPost(String post_content) {
        this.lastPostId++;
        this.posts.put(this.lastPostId, new Post(lastPostId, post_content));
        System.out.println("ADDED post: \n" + "\tuser: " + username +
                "\n\tID: " + lastPostId + "\n\tContent: " + post_content);

        this.lastUpdate = LocalTime.now().plusNanos(clockOffset);
    }

    public boolean deletePost(int postId) {
        Post deleted = this.posts.remove(postId);
        if (deleted != null) {
            System.out.println("DELETED post: \n"  + "\tuser: " + username +
                    "\n\tID: " + postId);
            this.lastUpdate = LocalTime.now().plusNanos(clockOffset);
            return true;
        }
        System.err.println("ERROR: Failed to delete post " + postId + " from " + username);
        return false;
    }

    public LocalTime getLastUpdate() { return lastUpdate; }

    public boolean updatePost(int postId, String post_content) {
        Post post = this.posts.get(postId);
        if (post != null && post.update(post_content)) {
            System.out.println("UPDATED post: \n"  + "\tuser: " + username +
                    "\n\tID: " + postId + "\n\tContent: " + post_content);
            this.lastUpdate = LocalTime.now().plusNanos(clockOffset);
            return true;
        }
        System.err.println("ERROR: Failed to delete post " + postId + " from " + username);
        return false;
    }

    public void save(File timelinesFolder) throws IOException {
        FileOutputStream fos = new FileOutputStream(timelinesFolder + File.separator + username);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);

        oos.close();
    }

    public boolean remove(File timelinesFolder) throws  IOException {
        return Files.deleteIfExists(Paths.get(timelinesFolder + File.separator + username)); //toDelete = new File(timelinesFolder + File.separator + username);
    }

    public String getUsername() { return this.username; }

    @Override
    public String toString() {
        return username + "'s Timeline:" +
                "\n\tLast Update:" + lastUpdate +
                "\n\tPosts: \n\t\t" + posts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timeline timeline = (Timeline) o;
        return lastPostId == timeline.lastPostId && Objects.equals(posts, timeline.posts) && Objects.equals(username, timeline.username) && Objects.equals(lastUpdate, timeline.lastUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(posts, lastPostId, username, lastUpdate);
    }
}
