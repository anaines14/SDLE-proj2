package main.model.message;
import java.io.Serializable;
import java.util.UUID;

public abstract class Message implements Serializable {
    private UUID id;

    public Message(UUID id) { this.id = id; }

    public abstract String getType();

    public UUID getId() { return this.id; }

    public String toString() {
        return getType();
    }
}
