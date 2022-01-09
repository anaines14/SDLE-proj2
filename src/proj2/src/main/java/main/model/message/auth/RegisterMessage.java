package main.model.message.auth;

import main.model.message.Message;

import java.util.UUID;

public class RegisterMessage extends Message {

    private String password;
    private String username;

    public RegisterMessage(UUID id, String username, String password) {
        super(id);
        this.username = username;
        this.password = password;
    }

    @Override
    public String getType() {
        return "REGISTER";
    }
}
