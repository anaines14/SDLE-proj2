package main.model.message.request;

import main.model.timelines.Timeline;

import java.util.UUID;

public class QueryHitMessage extends MessageRequest {
    private final Timeline timeline;
    public static final String type = "QUERY_HIT";

    public QueryHitMessage(UUID id, Timeline requestedTimeline) {
        super(id);
        this.timeline = requestedTimeline;
    }

    public Timeline getTimeline() {
        return timeline;
    }

    @Override
    public String getType() {
        return type;
    }
}
