package main.model.message.response;

import main.model.timelines.Timeline;
import org.zeromq.SocketType;
import org.zeromq.ZSocket;

import java.util.UUID;

public class SubHitMessage extends MessageResponse {
    public static final String type = "SUB_HIT";
    private final ZSocket socket;

    public SubHitMessage(UUID id) {
        super(id);
        this.socket = new ZSocket(SocketType.PUB); // TODO: do socket things
    }

    @Override
    public String getType() {
        return type;
    }
}
