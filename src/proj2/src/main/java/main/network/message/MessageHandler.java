package main.network.message;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.net.InetAddress;

public class MessageHandler implements Runnable {
    private InetAddress address;
    private String port;
    private ZMQ.Socket socket;

    public MessageHandler(InetAddress address, String port, ZContext context) {
        this.address = address;
        this.port = port;
        this.socket = context.createSocket(SocketType.REP);
    }

    public void handle(Message message) {
        System.out.println(message.toString());
    }

    @Override
    public void run() {
        this.socket.bind("tcp://*:" + port);
        while (!Thread.currentThread().isInterrupted()) {
            Message request = null;
            try {
                request = MessageBuilder.messageFromSocket(socket);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            assert request != null;
            handle(request);
        }
    }

    public void close() {
        this.socket.close();
    }
}
