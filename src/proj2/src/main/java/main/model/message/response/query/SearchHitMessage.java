package main.model.message.response.query;

import main.model.timelines.Post;

import java.util.List;
import java.util.UUID;

public class SearchHitMessage extends QueryResponseImpl{
    private final List<Post> posts;
    public static final String type = "SEARCH_HIT";

    public SearchHitMessage(UUID id, List<Post> requestedPosts) {
        super(id);
        this.posts = requestedPosts;
    }

    public List<Post> getPosts() {
        return posts;
    }

    @Override
    public String getType() {
        return type;
    }
}
