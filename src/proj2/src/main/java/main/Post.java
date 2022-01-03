package main;

import java.io.Serializable;
import java.time.LocalTime;

public class Post implements Serializable {
    private final LocalTime timestamp;
    private String content;

    public Post(String content) {
        this.timestamp = LocalTime.now();
        this.content = content;
    }

    public LocalTime getTime() { return this.timestamp; }

    public String getContent() { return this.content; }

    public boolean update(String newContent) {
        if (newContent.isEmpty()) {
            System.out.println("Empty content not allowed.");
            return false;
        }
        this.content = newContent;
        return true;
    }
}
