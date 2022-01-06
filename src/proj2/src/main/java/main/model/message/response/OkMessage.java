package main.model.message.response;

import main.model.neighbour.Host;

public class OkMessage extends MessageResponse {
    @Override
    public String getType() {
        return "OK";
    }
}
