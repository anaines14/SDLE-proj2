package main.model.message.response.query;

import main.model.message.Message;
import main.model.message.response.MessageResponse;

import java.util.UUID;

public abstract class QueryResponseImpl extends MessageResponse {

    public QueryResponseImpl(UUID id) {
        super(id);
    }

    @Override
    public String getType() {
        return null;
    }
}
