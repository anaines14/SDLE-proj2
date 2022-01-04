package main.network.message;

import main.network.PeerInfo;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.io.IOException;
import java.net.InetAddress;

// Recebe tds as msgs e dá-lhes handle
public class MessageHandler implements Runnable {
    private InetAddress address;
    private String port;
    private PeerInfo peerInfo;
    private ZMQ.Socket socket;
    private MessageSender sender;

    public MessageHandler(PeerInfo peerInfo, ZContext context, MessageSender sender) {
        this.address = peerInfo.address;
        this.port = peerInfo.port;
        this.peerInfo = peerInfo;
        this.socket = context.createSocket(SocketType.REP);
        this.sender = sender;
    }

    public void handle(Message message) {
        switch (message.getType()) {
            case "PING":
                handle((PingMessage) message);
                break;
            case "PONG":
                handle((PongMessage) message);
                break;

            default:
                break;
        }
    }

    public void handle(PingMessage message) {
        // Reply with a Pong message with our info
        PongMessage replyMsg = new PongMessage(peerInfo.getDegree(), peerInfo.hostCache, peerInfo.getStoredTimelines());
        sender.send(replyMsg, this.port);
    }

    public void handle(PongMessage message) {
        // Update/Add info that we have about a peer
        String senderUsername = message.username;
    }

    @Override
    public void run() {
        this.socket.bind("tcp://*:" + port);

        while (!Thread.currentThread().isInterrupted()) {
            Message request = null;
            try {
                request = MessageBuilder.messageFromSocket(socket);
            } catch (ZMQException e) {
                if (e.getErrorCode() == ZMQ.Error.ETERM.getCode() || // Context terminated
                    e.getErrorCode() == ZMQ.Error.EINTR.getCode()) // Interrupted
                    break;
                e.printStackTrace();
                this.close();
                return;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                this.close();
                return;
            }

            assert request != null;
            handle(request);
        }

        this.close();
    }

    public void close() {
        this.socket.setLinger(0);
        this.socket.close();
    }
}
