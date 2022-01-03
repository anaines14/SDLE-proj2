package main;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Timeline implements Serializable {
    private final List<Post> posts;
    private final String username;
    private LocalTime lastUpdate;

    public Timeline(String username) {
        this.posts = new ArrayList<>();
        this.username = username;
        this.lastUpdate = LocalTime.now();
    }

    public void addPost(Post post) {
        posts.add(post);
    }
}
