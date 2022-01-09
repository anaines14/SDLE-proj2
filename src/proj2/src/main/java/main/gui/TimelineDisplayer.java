package main.gui;

import main.model.timelines.Post;
import main.model.timelines.Timeline;

import java.time.format.DateTimeFormatter;

public class TimelineDisplayer {
    public TimelineDisplayer() {
    }

    public static void printPost(Post post) {
        System.out.println("=========================================");
        System.out.printf("%d\t%s\t\t%s\n", post.getId(), post.getUsername(), post.getTimestamp());
        System.out.println("\t" + post.getContent());
    }
}
