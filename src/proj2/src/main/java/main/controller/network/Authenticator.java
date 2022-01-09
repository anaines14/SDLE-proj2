package main.controller.network;

import main.controller.message.AuthMessageHandler;
import main.controller.message.MessageBuilder;
import main.model.message.Message;
import main.model.message.auth.*;
import org.zeromq.*;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Authenticator {
    private final InetAddress address;
    private ZContext context;
    private ZMQ.Socket socket;

    private String socketPort;
    private AuthMessageHandler authHandler;

    public Authenticator(InetAddress address) throws NoSuchAlgorithmException {
        this.context = new ZContext();
        this.socket = context.createSocket(SocketType.REP);
        this.authHandler = new AuthMessageHandler();


        String hostName = address.getHostName();
        this.address = address;
        this.socketPort = String.valueOf(socket.bindToRandomPort("tcp://" + hostName));

        System.out.println("BOUND TO " + "tcp://" + hostName + ":" + socketPort);

        run();
    }

    public static PublicKey requestPublicKey(String username, ZMQ.Socket socket) {

        try {
            socket.send(MessageBuilder.objectToByteArray(new GetPublicKeyMessage(UUID.randomUUID(),username)));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Message reply = null;
        try {
            reply = MessageBuilder.messageFromSocket(socket);
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }

        if(reply instanceof PublicKeyMessage){
                return ((PublicKeyMessage) reply).getPublicKey();
        }
        return null;
    }

    public static PrivateKey requestPrivateKey(String username, ZMQ.Socket socket) {

        try {
            socket.send(MessageBuilder.objectToByteArray(new GetPrivateKeyMessage(UUID.randomUUID(),username)));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Message reply = null;
        try {
            reply = MessageBuilder.messageFromSocket(socket);
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }

        if(reply instanceof PrivateKeyMessage){
            return ((PrivateKeyMessage) reply).getPrivateKey();
        }
        return null;
    }

    public static PrivateKey requestRegister(String username, String password, ZMQ.Socket socket) {
        try {
            socket.send(MessageBuilder.objectToByteArray(new RegisterMessage(UUID.randomUUID(),username,password)));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Message reply = null;
        try {
            reply = MessageBuilder.messageFromSocket(socket);
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }

        if(reply instanceof PrivateKeyMessage){
            return ((PrivateKeyMessage) reply).getPrivateKey();
        }
        return null;
    }

    public static PrivateKey requestLogin(String username, String password, ZMQ.Socket socket) {
        try {
            socket.send(MessageBuilder.objectToByteArray(new LoginMessage(UUID.randomUUID(),username,password)));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Message reply = null;
        try {
            reply = MessageBuilder.messageFromSocket(socket);
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }

        if(reply instanceof PrivateKeyMessage){
            return ((PrivateKeyMessage) reply).getPrivateKey();
        }
        return null;
    }

    private void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Message message =  MessageBuilder.messageFromSocket(socket);
                Message response = this.authHandler.handle(message);
                socket.send(MessageBuilder.objectToByteArray(response));
            } catch (ZMQException e) {
                if (e.getErrorCode() == ZMQ.Error.ETERM.getCode() || // Context terminated
                        e.getErrorCode() == ZMQ.Error.EINTR.getCode()) // Interrupted
                    break;
                e.printStackTrace();
                return;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public ZMQ.Socket getSocket() {
        return socket;
    }

    public String getSocketPort() {
        return socketPort;
    }

}
