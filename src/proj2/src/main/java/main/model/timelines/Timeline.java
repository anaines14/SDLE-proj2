package main.model.timelines;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.time.LocalTime;
import java.util.*;

public class Timeline implements Serializable, Comparable<Timeline> {
    private static final long serialVersionUID = 1L;

    private final Map<Integer, Post> posts;
    private int lastPostId;
    private final String username;
    private LocalTime lastUpdate;
    private final Long clockOffset;
    private final Cipher cipher;
    private boolean verification;

    public Timeline(String username, Long clockOffset) {
        this.posts = new HashMap<>();
        this.username = username;
        this.lastUpdate = LocalTime.now().plusNanos(clockOffset);
        this.lastPostId = 0;
        this.clockOffset = clockOffset;
        this.cipher = new Cipher();
    }

    public Post addPost(String post_content) {
        this.lastPostId++;
        Post res = new Post(lastPostId, username, post_content);
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

    public boolean hasSignature() {
        return this.cipher.hasSignature();
    }

    public void addSignature(PrivateKey privateKey) {
        this.cipher.addSignature(this.getTimelineContent(), privateKey);
    }

    public void verifySignature(PublicKey publicKey) {
        if(this.cipher.verifySignature(this.getTimelineContent(), publicKey)){
            this.verification = true;
            for(Integer pos : this.posts.keySet()){
                this.posts.get(pos).setVerification(true);
            }
        }
        else{
            this.verification = false;
        }
    }

    public String getUsername() { return this.username; }

    public List<Post> getPosts() {
        return new ArrayList<>(this.posts.values());
    }

    @Override
    public String toString() {
        return this.getTimelineContent() +
                "\n\tVerified: \n\t\t" + verification ;
    }

    public String getTimelineContent(){
        return username + "'s Timeline:" +
                "\n\tLast Update:" + lastUpdate +
                "\n\tPosts: \n\t\t" + posts;
    }

    public boolean isVerified() {
        return verification;
    }

    @Override
    public int compareTo(Timeline o) {
        return this.lastUpdate.compareTo(o.lastUpdate);
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

    public void setVerification(boolean verification) {
        this.verification = verification;
    }
}
