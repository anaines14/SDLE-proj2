package main.model.timelines;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Arrays;
import java.util.Objects;

public class Post implements Serializable, Comparable<Post> {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;
    private final int Id;
    private final LocalTime timestamp;
    private String content;
    private final Cipher cipher;
    private boolean verification;

    public Post(int Id, String username, String content) {
        this.username = username;
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
        this.cipher.addSignature(this.getPostContent(), privateKey);
    }

    public void verifySignature(PublicKey publicKey) {
        if(this.cipher.verifySignature(this.getPostContent(), publicKey)){
            this.verification = true;
        }
        else{
            this.verification = false;
        }
    }

    public boolean hasSignature() {
        return cipher.hasSignature();
    }

    public boolean matchesSearch(String search) {
        // convert to lower case and remove spaces for comparison
        String content_str = content.toLowerCase().replaceAll(" ", "");
        String search_str = search.toLowerCase().replaceAll(" ", "");
        return content_str.equals(search_str);
    }

    public int getId() {
        return Id;
    }

    public LocalTime getTimestamp() {
        return timestamp;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public String getPostContent(){
        return  "\t\tID: " + Id + ": " +
                "\n\t\tuser: " + username +
                "\n\t\t\tTimestamp: " + timestamp +
                "\n\t\t\tContent: '" + content + '\'';
    }

    @Override
    public String toString() {
        return  this.getPostContent() +
                "\n\tVerified: \n\t\t" + verification + "\n\tSign: \n\t\t" + Arrays.toString(this.cipher.getSign());
    }

    public boolean isVerified() {
        return verification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Id == post.Id && Objects.equals(timestamp, post.timestamp) && Objects.equals(content, post.content)
                && Objects.equals(username, post.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, timestamp, content);
    }


    @Override
    public int compareTo(Post o) {
        return this.timestamp.compareTo(o.timestamp);
    }

    public void setVerification(boolean verification) {
        this.verification = verification;
    }
}
