package main.network.message;

import main.network.PeerInfo;
import main.network.message.sender.Path;
import main.network.message.sender.Sender;

import java.io.Serializable;
import java.net.InetAddress;

public abstract class Message implements Serializable {
    public Path path;
    public String username;

    public Message(InetAddress senderAddress, String senderPort, String username) {
        this.path = new Path(senderAddress, senderPort);
        this.username = username;
    }

    public Message(PeerInfo info) {
        this(info.address, info.port, info.username);
    }

    public abstract String getType();

    @Override
    public String toString() {
        Sender lastSender = path.getLastSender();
        return "[" + getType() + "]" + " " + lastSender.getHostName() + ":" + lastSender;
    }
}
