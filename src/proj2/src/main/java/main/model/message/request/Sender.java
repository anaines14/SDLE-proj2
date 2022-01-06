package main.model.message.request;

import main.model.PeerInfo;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

public class Sender implements Serializable {
    private InetAddress address;
    private String port;

    public Sender(InetAddress address, String port) {
        this.address = address;
        this.port = port;
    }

    public Sender(PeerInfo peerInfo) {
        this.address = peerInfo.getAddress();
        this.port = peerInfo.getPort();
    }

    public String getPort() {
        return port;
    }

    public InetAddress getHostName() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sender)) return false;
        Sender sender = (Sender) o;
        return Objects.equals(address, sender.address) && Objects.equals(port, sender.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }
}
