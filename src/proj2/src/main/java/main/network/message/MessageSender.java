package main.network.message;

import main.network.PeerInfo;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MessageSender {
    // Each Peer has a MessageSender, and it sends all messages through it
    private final InetAddress senderAddress;
    private final String senderPort;
    private String username;
    private final ZContext context;

    public MessageSender(InetAddress senderAddress, String senderPort, String username, ZContext context) {
        this.senderAddress = senderAddress;
        this.senderPort = senderPort;
        this.username = username;
        this.context = context;
    }

    public MessageSender(PeerInfo peerInfo, ZContext context){
        this(peerInfo.getAddress(), peerInfo.getPort(), peerInfo.getUsername(), context);
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

    public MessageResponse sendRequest(Message message, String port, int timeout) {
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.setReceiveTimeOut(timeout);
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

    public Message sendRequest(Message message, String port) {
        return sendRequest(message, port, -1);
    }
}
