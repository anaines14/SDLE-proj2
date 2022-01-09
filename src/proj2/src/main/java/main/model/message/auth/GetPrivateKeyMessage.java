package main.model.message.auth;

import main.model.message.Message;

import java.util.UUID;

public class GetPrivateKeyMessage extends Message {
    private String username;

    public GetPrivateKeyMessage(UUID id, String username) {
        super(id);
        this.username = username;
    }

    @Override
    public String getType() {
        return "GETPRIVATEKEY";
    }
}
