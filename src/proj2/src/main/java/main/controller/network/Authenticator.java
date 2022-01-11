package main.controller.network;

import main.controller.message.MessageBuilder;
import main.model.SocketInfo;
import main.model.message.Message;
import main.model.message.auth.*;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

public class Authenticator {
    private ZMQ.Socket authSocket;

    public Authenticator(ZContext context) {
        this.authSocket = context.createSocket(SocketType.REQ);
    }

    public void connectToAuth(InetAddress address, String port) {
        String hostName = address.getHostName();
        this.authSocket.connect("tcp://" + hostName + ":" + port);
    }

    public Message send(Message message) {
        try {
            byte[] bytes = MessageBuilder.objectToByteArray(message);
            authSocket.send(bytes);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return null;
        }

        Message reply = null;
        try {
            reply = MessageBuilder.messageFromSocket(authSocket);
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }

        return reply;
    }

    public PublicKey requestPublicKey(String username) {
        Message request = new GetPublicKeyMessage(UUID.randomUUID(), username);
        Message reply = this.send(request);

        if(reply instanceof PublicKeyMessage){
            return ((PublicKeyMessage) reply).getPublicKey();
        }
        return null;
    }

    public void close(){
        this.authSocket.close();
    }

    public PrivateKey requestPrivateKey(String username) {
        Message request = new GetPrivateKeyMessage(UUID.randomUUID(), username);
        Message reply = this.send(request);

        if(reply instanceof PrivateKeyMessage){
            return ((PrivateKeyMessage) reply).getPrivateKey();
        }
        return null;
    }

    public PrivateKey requestRegister(String username, String password) {
        String hashedPassword = this.hashPassword(password);
        Message request = new RegisterMessage(UUID.randomUUID(),username,hashedPassword);
        Message reply = this.send(request);

        if(reply instanceof PrivateKeyMessage){
            return ((PrivateKeyMessage) reply).getPrivateKey();
        }
        return null;
    }

    public PrivateKey requestLogin(String username, String password) {
        String hashedPassword = this.hashPassword(password);
        Message request = new LoginMessage(UUID.randomUUID(),username,hashedPassword);
        Message reply = this.send(request);

        if(reply instanceof PrivateKeyMessage) {
            return ((PrivateKeyMessage) reply).getPrivateKey();
        }
        return null;
    }

    private String hashPassword(String password) {

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
        return generatedPassword;


    }
}


