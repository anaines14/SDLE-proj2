package main.network.message.sender;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Path {
    private final List<Sender> path;

    public Path(InetAddress senderAddress, String senderPort) {
        this.path = new ArrayList<>();
        this.path.add(new Sender(senderAddress, senderPort));
    }

    public Sender getLastSender() {
        return path.get(path.size() - 1);
    }
}
