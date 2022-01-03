package main.network;

import main.network.message.Message;
import main.network.message.MessageBuilder;
import main.network.message.MessageHandler;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class GnuNode {
    private InetAddress address;
    private String port;
    private ZContext context;
    private MessageHandler messageHandler;
    private Thread messageHandlerThread;

    public GnuNode(InetAddress address, String port) {
        this.context = new ZContext();
        this.address = address;
        this.port = port;

        this.messageHandler = new MessageHandler(address, port, context);
        this.messageHandlerThread = new Thread(messageHandler);
        this.messageHandlerThread.start();
    }

    // For testing
    public GnuNode(String address, String port) throws UnknownHostException {
        this(InetAddress.getByName(address), port);
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getPort() {
        return port;
    }

    public void send(Message message, String port) throws IOException {
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.connect("tcp://localhost:" + port); // TODO convert to address

        byte[] bytes = MessageBuilder.messageToByteArray(message);
        socket.send(bytes);
    }

    public void stop() {
        this.messageHandlerThread.interrupt();
    }
}
