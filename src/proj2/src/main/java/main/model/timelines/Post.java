package main.model.timelines;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;

public class Post implements Serializable, Comparable<Post> {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;
    private final int Id;
    private final LocalTime timestamp;
    private String content;

    public Post(int Id, String username, String content) {
        this.username = username;
        this.Id = Id;
        this.timestamp = LocalTime.now();
        this.content = content;
    }

    public boolean update(String newContent) {
        if (newContent.isEmpty()) {
            System.err.println("ERROR: Empty content not allowed.");
            return false;
        }
        this.content = newContent;
        return true;
    }

    public int getId() {
        return Id;
    }

    public LocalTime getTimestamp() {
        return timestamp;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return  "\t\tID: " + Id + ": " +
                "\n\t\tuser: " + username +
                "\n\t\t\tTimestamp: " + timestamp +
                "\n\t\t\tContent: '" + content + '\'';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Id == post.Id && Objects.equals(timestamp, post.timestamp) && Objects.equals(content, post.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, timestamp, content);
    }


    @Override
    public int compareTo(Post o) {
        return this.timestamp.compareTo(o.timestamp);
    }
}
