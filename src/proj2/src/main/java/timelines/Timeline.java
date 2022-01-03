package timelines;

import java.io.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public void addPost(Post post) {
        this.posts.put(post.getId(), post);
    }

    public void addPost(String post_content) {
        this.lastPostId++;
        this.posts.put(this.lastPostId, new Post(lastPostId, post_content));
    }

    public boolean deletePost(int postId) {
        Post deleted = this.posts.remove(postId);
        return deleted != null;
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
        return "Timeline{" +
                "posts=" + posts +
                ", username='" + username + '\'' +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}
