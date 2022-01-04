package main.network.message;

import java.net.InetAddress;

public class PingMessage extends Message {
    public PingMessage() {
        super();
    }

    @Override
    public String getType() {
        return "PING";
    }
}
