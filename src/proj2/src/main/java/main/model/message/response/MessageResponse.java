package main.model.message.response;

import main.model.message.Message;

import java.util.UUID;

public class MessageResponse extends Message {

    public MessageResponse(UUID id) { super(id); }

    public MessageResponse() { super(UUID.randomUUID()); }

    @Override
    public String getType() {
        return null;
    }
}
