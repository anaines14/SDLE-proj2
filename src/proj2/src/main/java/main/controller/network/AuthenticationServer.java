package main.controller.network;

import main.controller.message.AuthMessageHandler;
import main.controller.message.MessageBuilder;
import main.model.message.Message;
import main.model.message.auth.*;
import org.zeromq.*;

import java.io.IOException;
import java.net.InetAddress;
import java.security.*;
import java.util.UUID;

public class AuthenticationServer {
    private final InetAddress address;
    private ZContext context;
    private ZMQ.Socket socket;

    private String socketPort;
    private Thread thread;
    private AuthMessageHandler authHandler;

    public AuthenticationServer(InetAddress address){
        this.context = new ZContext();
        this.socket = context.createSocket(SocketType.REP);
        this.authHandler = new AuthMessageHandler();


        String hostName = address.getHostName();

        this.thread = new Thread(this::run);
        this.address = address;
        this.socketPort = String.valueOf(socket.bindToRandomPort("tcp://" + hostName));

        System.out.println("BOUND TO " + "tcp://" + hostName + ":" + socketPort);

    }

    public void execute() {
        this.thread.start();
    }

    public void run() {
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

    public InetAddress getAddress() {
        return address;
    }

    public String getSocketPort() {
        return socketPort;
    }

}
