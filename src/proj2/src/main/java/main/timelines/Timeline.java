package main.timelines;

import java.io.*;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class Timeline implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<Integer, Post> posts;
    private int lastPostId;
    private final String username;
    private LocalTime lastUpdate;

    public Timeline(String username) {
        this.posts = new HashMap<>();
        this.username = username;
        this.lastUpdate = LocalTime.now();
        this.lastPostId = 0;
    }

    public void addPost(String post_content) {
        this.lastPostId++;
        this.posts.put(this.lastPostId, new Post(lastPostId, post_content));
        System.out.println("ADDED post: \n" + "\tuser: " + username +
                "\n\tID: " + lastPostId + "\n\tContent: " + post_content);

    }

    public boolean deletePost(int postId) {
        Post deleted = this.posts.remove(postId);
        if (deleted != null) {
            System.out.println("DELETED post: \n"  + "\tuser: " + username +
                    "\n\tID: " + postId);
            return true;
        }
        System.err.println("ERROR: Failed to delete post " + postId + " from " + username);
        return false;
    }

    public boolean updatePost(int postId, String post_content) {
        Post post = this.posts.get(postId);
        if (post != null && post.update(post_content)) {
            System.out.println("UPDATED post: \n"  + "\tuser: " + username +
                    "\n\tID: " + postId + "\n\tContent: " + post_content);
            return true;
        }
        System.err.println("ERROR: Failed to delete post " + postId + " from " + username);
        return false;
    }

    public void save(File timelines_folder) throws IOException {
        FileOutputStream fos = new FileOutputStream(timelines_folder + File.separator + username);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);

        oos.close();
    }

    public String getUsername() { return this.username; }

    @Override
    public String toString() {
        return username + "'s Timeline:" +
                "\n\tLast Update:" + lastUpdate +
                "\n\tPosts: \n\t\t" + posts;
    }
}
