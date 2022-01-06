package main.model.message.request;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Path implements Serializable {
    private final List<Sender> path;

    public Path() {
        this.path = new ArrayList<>();
    }

    public void addSender(Sender sender) {
        path.add(sender);
    }

    public boolean isEmpty() {
        return path.isEmpty();
    }

    public boolean isInPath(Sender lookFor) {
        return path.contains(lookFor);
    }

    public Sender getLastSender() {
        return path.get(path.size() - 1);
    }

    public Sender getOriginalSender() {
        return path.get(0);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < path.size() - 1; ++i)
            res.append(path.get(i).getPort()).append("-");
        if (!path.isEmpty())
            res.append(path.get(path.size() - 1).getPort());
        return res.toString();
    }
}
