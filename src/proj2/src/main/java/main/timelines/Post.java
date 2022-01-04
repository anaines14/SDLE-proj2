package main.timelines;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;

public class Post implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int Id;
    private final LocalTime timestamp;
    private String content;

    public Post(int Id, String content) {
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

    @Override
    public String toString() {
        return  "\n\t\tID: " + Id + ": " +
                "\n\t\t\tTimestamp: " + timestamp +
                "\n\t\t\tContent: '" + content + '\'';
    }
}
