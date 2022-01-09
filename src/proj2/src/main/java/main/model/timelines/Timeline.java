package main.model.timelines;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Timeline implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<Integer, Post> posts;
    private int lastPostId;
    private final String username;
    private LocalTime lastUpdate;
    private final Long clockOffset;
    private byte[] sign = null;

    public Timeline(String username, Long clockOffset) {
        this.posts = new HashMap<>();
        this.username = username;
        this.lastUpdate = LocalTime.now().plusNanos(clockOffset);
        this.lastPostId = 0;
        this.clockOffset = clockOffset;
    }

    public Post addPost(String post_content) {
        this.lastPostId++;
        Post res = new Post(lastPostId, post_content);
        this.posts.put(this.lastPostId, res);
        this.lastUpdate = LocalTime.now().plusNanos(clockOffset);
        return res;
    }

    public boolean deletePost(int postId) {
        Post deleted = this.posts.remove(postId);
        if (deleted != null) {
            this.lastUpdate = LocalTime.now().plusNanos(clockOffset);
            return true;
        }
        System.err.println("ERROR: Failed to delete post " + postId + " from " + username);
        return false;
    }

    public boolean hasSignature(){
        return sign != null;
    }

    public LocalTime getLastUpdate() { return lastUpdate; }

    public boolean updatePost(int postId, String post_content) {
        Post post = this.posts.get(postId);
        if (post != null && post.update(post_content)) {
            this.lastUpdate = LocalTime.now().plusNanos(clockOffset);
            return true;
        }
        System.err.println("ERROR: Failed to delete post " + postId + " from " + username);
        return false;
    }

    public void save(File timelinesFolder) throws IOException {
        FileOutputStream fos = new FileOutputStream(timelinesFolder + File.separator + username);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);

        oos.close();
    }

    public boolean remove(File timelinesFolder) throws  IOException {
        return Files.deleteIfExists(Paths.get(timelinesFolder + File.separator + username)); //toDelete = new File(timelinesFolder + File.separator + username);
    }

    public String getUsername() { return this.username; }

    public void addSignature(PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withDSA");
            signature.initSign(privateKey);
            signature.update(this.toString().getBytes());
            sign = signature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
    }


    public boolean verifySignature(PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withDSA");
            signature.initVerify(publicKey);
            signature.update(this.toString().getBytes());
            return signature.verify(sign);
        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        return username + "'s Timeline:" +
                "\n\tLast Update:" + lastUpdate +
                "\n\tPosts: \n\t\t" + posts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timeline timeline = (Timeline) o;
        return lastPostId == timeline.lastPostId && Objects.equals(posts, timeline.posts) && Objects.equals(username, timeline.username) && Objects.equals(lastUpdate, timeline.lastUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(posts, lastPostId, username, lastUpdate);
    }
}
