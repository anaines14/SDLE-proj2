package main.network.message;

import main.network.PeerInfo;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.net.InetAddress;

public class MessageSender {
    // TODO thread pool para isto
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
        this(peerInfo.address, peerInfo.port, peerInfo.username, context);
    }

    public void send(Message message, String port) {
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.connect("tcp://localhost:" + port); // TODO convert to address

        message.senderAddress = senderAddress;
        message.senderPort = senderPort;
        message.username = username;
        byte[] bytes = new byte[0];
        try {
            bytes = MessageBuilder.messageToByteArray(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.send(bytes);
        System.out.println("Sending " + message.toString() + " to " + port);
        socket.close();
    }
}
