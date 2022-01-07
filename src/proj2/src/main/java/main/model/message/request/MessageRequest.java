package main.model.message.request;

import main.model.message.Message;

import java.util.UUID;

public abstract class MessageRequest extends Message {
    private UUID id;

    public MessageRequest(UUID id) {
        this.id = id;
    }

    public MessageRequest() {
        this(UUID.randomUUID());
    }

    public UUID getId() {
        return id;
    }
}
