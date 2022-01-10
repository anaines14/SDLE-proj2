package main.model.message.auth;

import main.model.message.Message;

import java.util.UUID;

public class OperationFailedMessage extends Message {

    public OperationFailedMessage(UUID id) {
        super(id);
    }

    @Override
    public String getType() {
        return "FAILED";
    }

}
