package main.model.message.auth;

import main.model.message.Message;

import java.security.PublicKey;
import java.util.UUID;

public class PublicKeyMessage extends Message {

    private PublicKey publicKey;

    public PublicKeyMessage(UUID id, PublicKey publicKey) {
        super(id);
        this.publicKey = publicKey;
    }

    @Override
    public String getType() {
        return "PUBLICKEYOK";
    }


    public PublicKey getPublicKey() {
        return publicKey;
    }

}
