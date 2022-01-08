package main.model.message.response.query;

import java.util.UUID;

public class SubHitMessage extends QueryResponseImpl {
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
