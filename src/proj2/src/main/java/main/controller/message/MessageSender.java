package main.controller.message;

import main.model.PeerInfo;
import main.model.message.Message;
import main.model.message.request.MessageRequest;
import main.model.message.response.MessageResponse;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public class MessageSender {
    // Each Peer has a MessageSender, and it sends all messages through it
    private final InetAddress senderAddress;
    private final String senderPort;
    private String username;
    private final ZContext context;
    private int maxRetries;
    private int receiveTimeout;

    public MessageSender(InetAddress senderAddress, String senderPort,
                         String username, int maxRetries, int receiveTimeout, ZContext context) {
        this.senderAddress = senderAddress;
        this.senderPort = senderPort;
        this.username = username;
        this.maxRetries = maxRetries;
        this.receiveTimeout = receiveTimeout;
        this.context = context;
    }

    public MessageSender(PeerInfo peerInfo, int maxRetries, int receiveTimeout, ZContext context){
        this(peerInfo.getAddress(), peerInfo.getPort(), peerInfo.getUsername(), maxRetries, receiveTimeout, context);
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

    private MessageResponse sendRequest(MessageRequest message, String port) {
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.setReceiveTimeOut(receiveTimeout);
        String uuid = username + "-" + UUID.randomUUID();
        // We use UUID's so that, when creating multiple sockets per user we don't override them

        socket.setIdentity(uuid.getBytes(StandardCharsets.UTF_8));
        socket.connect("tcp://localhost:" + port); // TODO convert to address

        this.send(message, socket);
        System.out.println(username + " SENT[" + message.getType() + "]: " + port);

        try {
            return (MessageResponse) MessageBuilder.messageFromSocket(socket);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        socket.close();
        context.destroySocket(socket);
        return null;
    }

    public MessageResponse sendRequestNTimes(MessageRequest message, String port) {
        int i = 0;
        boolean done = false;
        while (i < this.maxRetries && !done) {
            MessageResponse response = this.sendRequest(message, port);
            if (response != null && Objects.equals(response.getType(), "PONG")) {
                return response;
            }
            ++i;
        }

        return null;
    }
}
