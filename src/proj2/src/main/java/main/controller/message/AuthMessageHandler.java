package main.controller.message;

import main.model.PeerInfo;
import main.model.message.*;
import main.model.message.auth.*;
import main.model.message.request.*;
import main.model.message.request.query.QueryMessage;
import main.model.message.request.query.QueryMessageImpl;
import main.model.message.request.query.SubMessage;
import main.model.message.response.*;
import main.model.message.response.query.QueryHitMessage;
import main.model.message.response.query.SubHitMessage;
import main.model.neighbour.Neighbour;
import main.model.timelines.Timeline;
import main.model.timelines.TimelineInfo;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.security.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.IntStream;

import static main.Peer.MAX_RANDOM_NEIGH;

// Dá handle só a mensagens que iniciam requests (PING)
public class AuthMessageHandler {
    Map<String, KeyPair> usernameToKeys;
    Map<String, String> usernamePassword;
    KeyPairGenerator keyPairGenerator;

    public AuthMessageHandler() {
        usernamePassword = new HashMap<>();
        usernameToKeys = new HashMap<>();
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyPairGenerator.initialize(2048);
    }
    public Message handle(Message message) {
        switch (message.getType()) {
            case "LOGIN" -> handle((LoginMessage) message);
            case "REGISTER" -> handle((RegisterMessage) message);
            case "GETPRIVATEKEY" -> handle((GetPrivateKeyMessage) message);
            case "GETPUBLICKEY" -> handle((GetPublicKeyMessage) message);
            default -> {
            }
        }
        return new OperationFailedMessage(UUID.randomUUID());
    }


    private Message handle(LoginMessage message) {
        String username = message.getUsername();
        UUID uuid = UUID.randomUUID();
        if(login(username,message.getPassword())){
            return new PrivateKeyMessage(uuid,getPrivateKey(username));
        }
        return new OperationFailedMessage(uuid);

    }

    private Message handle(RegisterMessage message) {
        String username = message.getUsername();
        UUID uuid = UUID.randomUUID();
        if(register(username, message.getPassword())){
            return new PrivateKeyMessage(uuid, getPrivateKey(username));
        }
        return new OperationFailedMessage(uuid);
    }

    private Message handle(GetPrivateKeyMessage message) {
        String username = message.getUsername();
        UUID uuid = UUID.randomUUID();
        return new PrivateKeyMessage(uuid,getPrivateKey(username));
    }

    private Message handle(GetPublicKeyMessage message) {
        String username = message.getUsername();
        UUID uuid = UUID.randomUUID();
        return new PublicKeyMessage(uuid,getPublicKey(username));
    }


    public boolean login(String username, String password){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(password.getBytes());
        byte[] bytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        String generatedPassword = sb.toString();
        System.out.println("generated pwd " + generatedPassword);

        // username not registered
        if(usernamePassword.get(username) == null){
            System.out.println("User not registered");
            return false;
        }
        //passwords match logging in
        if(usernamePassword.get(username).equals(generatedPassword)){
            System.out.println("Passwords match, easy clap");
            return true;
        }
        return false;
    }

    public boolean register(String username, String password){

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //Not registered
        if(!usernamePassword.containsKey(username)){
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for(int i = 0;i< bytes.length; i++){
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100,16).substring(1));
            }
            String generatedPassword = sb.toString();
            System.out.println("generated pwd " + generatedPassword);
            usernamePassword.put(username,generatedPassword);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            usernameToKeys.put(username,keyPair);
            return true;
        }
        else{
            System.out.println("Already registered");
            return false;
        }

    }

    public PrivateKey getPrivateKey(String username){
        return usernameToKeys.get(username).getPrivate();
    }

    public PublicKey getPublicKey(String username){
        return usernameToKeys.get(username).getPublic();
    }


}
