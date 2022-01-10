package main.model.message.auth;

import main.model.message.Message;

import java.security.PrivateKey;
import java.util.UUID;

public class PrivateKeyMessage extends Message {
    private PrivateKey privateKey;

    public PrivateKeyMessage(UUID id, PrivateKey privateKey) {
        super(id);
        this.privateKey = privateKey;
    }

    @Override
    public String getType() {
        return "PRIVATEKEYOK";
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }


}
