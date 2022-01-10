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
        Message request = new RegisterMessage(UUID.randomUUID(),username,password);
        Message reply = this.send(request);

        if(reply instanceof PrivateKeyMessage){
            return ((PrivateKeyMessage) reply).getPrivateKey();
        }
        return null;
    }

    public PrivateKey requestLogin(String username, String password) {
        Message request = new LoginMessage(UUID.randomUUID(),username,password);
        Message reply = this.send(request);

        if(reply instanceof PrivateKeyMessage) {
            return ((PrivateKeyMessage) reply).getPrivateKey();
        }
        return null;
    }
}
