package main.network.message;

import main.network.PeerInfo;

import java.io.Serializable;
import java.net.InetAddress;

public abstract class Message implements Serializable {
    public InetAddress senderAddress;
    public String senderPort;
    public String username;

    public Message(InetAddress senderAddress, String senderPort, String username) {
        this.senderAddress = senderAddress;
        this.senderPort = senderPort;
        this.username = username;
    }

    public Message(PeerInfo info) {
        this(info.address, info.port, info.username);
    }

    public abstract String getType();

    @Override
    public String toString() {
        return "[" + getType() + "]" + " " + senderAddress.getHostName() + ":" + senderPort;
    }
}
