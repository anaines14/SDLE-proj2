package main.model.message.response.query;

import main.model.message.request.MessageRequest;
import main.model.message.response.MessageResponse;
import main.model.timelines.Timeline;

import java.util.UUID;

public class QueryHitMessage extends QueryResponseImpl {
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
