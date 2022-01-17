package main.model.message.response;

import java.util.UUID;

public class SubPong extends MessageResponse {
    public SubPong(UUID id) {
        super(id);
    }

    @Override
    public String getType() {
        return "SUB_PONG";
    }
}
