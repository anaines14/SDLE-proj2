package main.model.message.response;

import main.model.message.request.MessageRequest;
import main.model.message.response.MessageResponse;
import main.model.timelines.Timeline;

import java.util.UUID;

public class QueryHitMessage extends MessageResponse {
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
