package main.model.message.sender;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Path implements Serializable {
    private final List<Sender> path;

    public Path(InetAddress senderAddress, String senderPort) {
        this.path = new ArrayList<>();
        this.path.add(new Sender(senderAddress, senderPort));
    }

    public Sender getLastSender() {
        return path.get(path.size() - 1);
    }

    public void addSender(InetAddress address, String port) {
        path.add(new Sender(address, port));
    }
}
