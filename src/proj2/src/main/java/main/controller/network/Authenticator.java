package main.controller.network;

import main.controller.message.AuthMessageHandler;
import main.controller.message.MessageBuilder;
import main.model.message.Message;
import org.zeromq.*;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Authenticator {
    private ZContext context;
    private ZMQ.Socket socket;

    private String socketPort;
    private AuthMessageHandler authHandler;

    Authenticator(ZContext context, InetAddress address) throws NoSuchAlgorithmException {
        this.context = context;
        this.socket = context.createSocket(SocketType.REP);
        this.authHandler = new AuthMessageHandler();


        String hostName = address.getHostName();
        this.socketPort = String.valueOf(socket.bindToRandomPort("tcp://" + hostName));

        System.out.println("BOUND TO " + "tcp://" + hostName + ":" + socketPort);

        run();
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

}
