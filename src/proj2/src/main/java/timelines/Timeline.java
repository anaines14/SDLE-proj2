package timelines;

import java.io.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Timeline implements Serializable {
    private static final long serialVersionUID = 1L;

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
