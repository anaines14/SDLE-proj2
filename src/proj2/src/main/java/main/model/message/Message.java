package main.model.message;

import main.model.message.sender.Path;
import main.model.message.sender.Sender;
import main.model.neighbour.Host;

import java.io.Serializable;
import java.net.InetAddress;

public abstract class Message implements Serializable {
    public Path path;

    public Message(InetAddress senderAddress, String senderPort) {
        this.path = new Path(senderAddress, senderPort);
    }

    public Message(Host host) {
        this(host.getAddress(), host.getPort());
    }

    public abstract String getType();

    public Sender getLastSender() {
        return path.getLastSender();
    }

    @Override
    public String toString() {
        Sender lastSender = getLastSender();
        return lastSender.getPort() + ": " + "[" + getType() + "]";
    }
}
