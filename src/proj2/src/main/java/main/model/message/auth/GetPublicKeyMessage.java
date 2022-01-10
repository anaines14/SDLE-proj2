package main.model.message.auth;

import main.model.message.Message;

import java.util.UUID;

public class GetPublicKeyMessage extends Message {

    private String username;

    public GetPublicKeyMessage(UUID id, String username) {
        super(id);
        this.username = username;
    }

    @Override
    public String getType() {
        return "GETPUBLICKEY";
    }

    public String getUsername() {
        return username;
    }
}
