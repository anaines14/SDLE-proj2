package main.model.timelines;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalTime;
import java.util.Objects;

public class Post implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int Id;
    private final LocalTime timestamp;
    private String content;
    private final Cipher cipher;

    public Post(int Id, String content) {
        this.Id = Id;
        this.timestamp = LocalTime.now();
        this.content = content;
        this.cipher = new Cipher();
    }

    public boolean update(String newContent) {
        if (newContent.isEmpty()) {
            System.err.println("ERROR: Empty content not allowed.");
            return false;
        }
        this.content = newContent;
        return true;
    }

    public void addSignature(PrivateKey privateKey) {
        this.cipher.addSignature(this.toString(), privateKey);
    }

    public boolean verifySignature(PublicKey publicKey) {
        return this.cipher.verifySignature(this.toString(), publicKey);
    }

    public boolean hasSignature(){
        return cipher.hasSignature();
    }

    @Override
    public String toString() {
        return  "\t\tID: " + Id + ": " +
                "\n\t\t\tTimestamp: " + timestamp +
                "\n\t\t\tContent: '" + content + '\'';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Id == post.Id && Objects.equals(timestamp, post.timestamp) && Objects.equals(content, post.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, timestamp, content);
    }
}
