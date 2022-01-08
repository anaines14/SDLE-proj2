package main.controller.message;

import main.gui.Observer;
import main.model.PeerInfo;
import main.model.message.Message;
import main.model.message.response.QueryHitMessage;
import main.model.message.request.query.QueryMessage;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.*;

public class MessageSender {
    // Each Peer has a MessageSender, and it sends all messages through it
    private String username;
    private final ZContext context;
    private int maxRetries;
    private int receiveTimeout;
    private Observer observer;
    private String port;

    // Used for testng
    private static List<String> ignoredMessages = new ArrayList<>();
    public static void removeIgnoredMsg(String msgType) {
        ignoredMessages.remove(msgType);
    }
    public static void addIgnoredMsg(String msgType) {
        ignoredMessages.add(msgType);
    }

    public MessageSender(String username, String port, int maxRetries, int receiveTimeout, ZContext context) {
        this.username = username;
        this.port = port;
        this.maxRetries = maxRetries;
        this.receiveTimeout = receiveTimeout;
        this.context = context;
    }

    public MessageSender(PeerInfo peerInfo, int maxRetries, int receiveTimeout, ZContext context){
        this(peerInfo.getUsername(), peerInfo.getPort(), maxRetries, receiveTimeout, context);
    }

    public void send(Message message, ZMQ.Socket socket) {
        byte[] bytes = new byte[0];
        try {
            bytes = MessageBuilder.messageToByteArray(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.send(bytes);
    }

    private String sendMessage(Message message, String port) {
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.setReceiveTimeOut(receiveTimeout);
        socket.connect("tcp://localhost:" + port); // TODO convert to address

        this.send(message, socket);

        // notify observer
        this.notify(message, port);

        if (!ignoredMessages.contains(message.getType()))
            System.out.println(username + " SENT[" + message.getType() + "]: " + port);

        String res = socket.recvStr();

        socket.close();
        context.destroySocket(socket);
        return res;
    }

    public boolean sendMessageNTimes(Message message, String port) {
        int i = 0;
        boolean done = false;
        while (i < this.maxRetries && !done) {
            String response = this.sendMessage(message, port);
            if (response != null && response.equals("OK")) {
                done = true;
            }
            ++i;
        }

        if (!done) {
            System.out.println("Failed getting response to [" + message.getType() + "]" + port);
            return false;
        }
        return true;
    }

    // observers
    public void subscribe(Observer o) {
        this.observer = o;
    }

    private void notify(Message message, String port) {
        if (observer == null) return;
        String type = message.getType();

        switch (type) {
            case QueryHitMessage.type -> this.observer.newQueryHitUpdate(this.port, port);
            case QueryMessage.type -> this.observer.newQueryUpdate(this.port, port);
        }
    }
}
