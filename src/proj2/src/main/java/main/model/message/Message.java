package main.model.message;
import java.io.Serializable;

public abstract class Message implements Serializable {
    public abstract String getType();

    public String toString() {
        return getType();
    }
}
