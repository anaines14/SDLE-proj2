package main.model.message.response;

import main.model.timelines.Timeline;
import org.zeromq.SocketType;
import org.zeromq.ZSocket;

import java.util.UUID;

public class SubHitMessage extends MessageResponse {
    public static final String type = "SUB_HIT";
    private final String port;

    public SubHitMessage(UUID id) {
        super(id);
        this.port = "123123";
    }

    public String getPort() {
        return port;
    }

    @Override
    public String getType() {
        return type;
    }
}
