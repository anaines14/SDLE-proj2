package main.network.message;

import java.io.Serializable;
import java.net.InetAddress;

public abstract class Message implements Serializable {
    public InetAddress senderAddress;
    public String senderPort;
    public String username;

    public abstract String getType();

    @Override
    public String toString() {
        return "[" + getType() + "]" + " " + senderAddress.getHostName() + ":" + senderPort;
    }
}
