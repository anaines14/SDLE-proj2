package main.model.message.auth;

import main.model.message.Message;

import java.util.UUID;

public class LoginMessage extends Message {
    String username;
    String password;

    public LoginMessage(UUID id,String username, String password) {
        super(id);
        this.username = username;
        this.password = password;
    }

    @Override
    public String getType() {
        return "LOGIN";
    }


    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
