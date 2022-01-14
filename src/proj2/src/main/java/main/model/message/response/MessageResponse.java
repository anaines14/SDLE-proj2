package main.model.message.response;

import main.model.message.Message;

import java.util.UUID;

public abstract class MessageResponse extends Message {

    public MessageResponse(UUID id) { super(id); }

    public MessageResponse() { super(UUID.randomUUID()); }
}
